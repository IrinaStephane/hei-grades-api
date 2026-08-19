package school.hei.api.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.User;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;

@Component
@AllArgsConstructor
public class PdfTranscriptGenerator {

  private final GroupFlowRepository groupFlowRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  public File generate(User student, Integer year) {
    try {
      var courses = computeCourses(student, year);
      double generalAverage = computeGeneralAverage(courses);
      int totalCredits = computeTotalCredits(courses);
      String status = computeStatus(student.getId());
      String html = buildHtml(student, year, courses, generalAverage, totalCredits, status);
      File file = File.createTempFile("transcript-" + student.getId(), ".pdf");
      try (var os = new FileOutputStream(file)) {
        var builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();
      }
      return file;
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate transcript PDF", e);
    }
  }

  private List<CourseTranscript> computeCourses(User student, Integer year) {
    var flows = groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(student.getId());
    List<String> groupIds = flows.stream().map(f -> f.getGroup().getId()).distinct().toList();

    List<CourseAssignment> assignments = new ArrayList<>();
    for (String groupId : groupIds) {
      assignments.addAll(courseAssignmentRepository.findByGroupId(groupId));
    }

    Map<String, CourseAssignment> uniqueMap = new HashMap<>();
    for (CourseAssignment a : assignments) {
      uniqueMap.putIfAbsent(a.getId(), a);
    }
    assignments = new ArrayList<>(uniqueMap.values());

    if (year != null) {
      assignments = assignments.stream().filter(a -> a.getYear().equals(year)).toList();
    }

    List<CourseTranscript> result = new ArrayList<>();
    for (CourseAssignment assignment : assignments) {
      var course = assignment.getCourse();
      var exams = examRepository.findByCourseAssignmentId(assignment.getId());
      double finalGrade =
          exams.stream()
              .mapToDouble(
                  exam -> {
                    var grades = gradeRepository.findByExamId(exam.getId());
                    var studentGrade =
                        grades.stream()
                            .filter(g -> g.getStudentId().equals(student.getId()))
                            .findFirst()
                            .map(g -> g.getScore())
                            .orElse(0.0);
                    return studentGrade * exam.getCoefficient();
                  })
              .sum();
      result.add(
          new CourseTranscript(
              course.getCode(),
              course.getTitle(),
              course.getCredits(),
              finalGrade,
              finalGrade >= 10));
    }

    result.sort(Comparator.comparing(CourseTranscript::courseCode));
    return result;
  }

  private double computeGeneralAverage(List<CourseTranscript> courses) {
    if (courses.isEmpty()) {
      return 0;
    }
    double sumCredits = courses.stream().mapToInt(CourseTranscript::credits).sum();
    double weightedSum = courses.stream().mapToDouble(c -> c.finalGrade() * c.credits()).sum();
    return sumCredits == 0 ? 0 : weightedSum / sumCredits;
  }

  private int computeTotalCredits(List<CourseTranscript> courses) {
    return courses.stream()
        .filter(CourseTranscript::passed)
        .mapToInt(CourseTranscript::credits)
        .sum();
  }

  private String computeStatus(String studentId) {
    var grades = gradeRepository.findByStudentId(studentId);
    if (grades.isEmpty()) {
      return "PROVISOIRE";
    }
    boolean allFinal = grades.stream().allMatch(g -> g.isFinal());
    return allFinal ? "COMPLET" : "PROVISOIRE";
  }

  private String buildHtml(
      User student,
      Integer year,
      List<CourseTranscript> courses,
      double generalAverage,
      int totalCredits,
      String status) {
    String scope = year == null ? "cumulé (3 ans)" : "année " + year;
    String escapedFirstName = HtmlUtils.htmlEscape(student.getFirstName());
    String escapedLastName = HtmlUtils.htmlEscape(student.getLastName());

    StringBuilder sb = new StringBuilder();
    sb.append("<html><head><style>");
    sb.append("table { border-collapse: collapse; width: 100%; }");
    sb.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }");
    sb.append("th { background-color: #f2f2f2; }");
    sb.append("</style></head><body>");
    sb.append("<h1>Relevé de notes — ")
        .append(escapedFirstName)
        .append(" ")
        .append(escapedLastName)
        .append("</h1>");
    sb.append("<p>Période : ").append(scope).append("</p>");
    sb.append("<p>Statut : ").append(status).append("</p>");
    sb.append("<table><thead><tr>");
    sb.append("<th>Code</th><th>Intitulé</th><th>Crédits</th><th>Note finale</th><th>Statut</th>");
    sb.append("</tr></thead><tbody>");
    for (CourseTranscript c : courses) {
      sb.append("<tr>");
      sb.append("<td>").append(HtmlUtils.htmlEscape(c.courseCode())).append("</td>");
      sb.append("<td>").append(HtmlUtils.htmlEscape(c.courseTitle())).append("</td>");
      sb.append("<td>").append(c.credits()).append("</td>");
      sb.append("<td>").append(String.format("%.2f", c.finalGrade())).append("</td>");
      sb.append("<td>").append(c.passed() ? "Admis" : "Échoué").append("</td>");
      sb.append("</tr>");
    }
    sb.append("</tbody></table>");
    sb.append("<p>Moyenne générale : ")
        .append(String.format("%.2f", generalAverage))
        .append("</p>");
    sb.append("<p>Crédits obtenus : ").append(totalCredits).append("</p>");
    sb.append("</body></html>");
    return sb.toString();
  }

  private record CourseTranscript(
      String courseCode, String courseTitle, int credits, double finalGrade, boolean passed) {}
}
