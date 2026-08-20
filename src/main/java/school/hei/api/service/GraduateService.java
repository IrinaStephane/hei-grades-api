package school.hei.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;

@Service
@AllArgsConstructor
public class GraduateService {

  private final PromotionRepository promotionRepository;
  private final StudentCurriculumService studentCurriculumService;
  private final GroupRepository groupRepository;
  private final GroupFlowRepository groupFlowRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  @Builder
  @Getter
  public static class CourseResult {
    private final String courseId;
    private final int credits;
    private final double finalGrade;
  }

  @Builder
  @Getter
  public static class Graduate {
    private final String studentId;
    private final String firstName;
    private final String lastName;
    private final String path;
    private final double generalAverage;
    private final List<CourseResult> results;
    private final List<Integer> resultYears;
  }

  private record StudentInfo(String id, String firstName, String lastName) {}

  public List<Graduate> getGraduates(String promotionId, String path) {
    var promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(() -> new NotFoundException("Promotion " + promotionId + " not found"));
    List<Integer> schoolYears = schoolYearsOf(promotion);
    if (schoolYears.isEmpty()) {
      return List.of();
    }

    return studentsOfPromotion(promotionId, path).stream()
        .map(student -> toGraduate(student, path, schoolYears))
        .filter(g -> !g.getResults().isEmpty())
        .filter(g -> g.getResultYears().containsAll(schoolYears))
        .filter(g -> g.getResults().stream().allMatch(r -> r.getFinalGrade() >= 10))
        .sorted(Comparator.comparingDouble(Graduate::getGeneralAverage).reversed())
        .toList();
  }

  private Graduate toGraduate(StudentInfo student, String path, List<Integer> schoolYears) {
    Path pathEnum = path == null ? null : Path.valueOf(path);

    List<CourseResult> results = new ArrayList<>();
    List<Integer> resultYears = new ArrayList<>();
    for (int year : schoolYears) {
      var assignments = studentCurriculumService.assignmentsForYear(student.id(), year, pathEnum);
      if (assignments.isEmpty()) {
        continue;
      }
      resultYears.add(year);
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
                              .filter(g -> g.getStudentId().equals(student.id()))
                              .findFirst()
                              .map(g -> g.getScore())
                              .orElse(0.0);
                      return studentGrade * exam.getCoefficient();
                    })
                .sum();
        results.add(
            CourseResult.builder()
                .courseId(course.getId())
                .credits(course.getCredits())
                .finalGrade(finalGrade)
                .build());
      }
    }

    double sumCredits = results.stream().mapToInt(CourseResult::getCredits).sum();
    double weightedSum =
        results.stream().mapToDouble(r -> r.getFinalGrade() * r.getCredits()).sum();
    double average = sumCredits == 0 ? 0 : weightedSum / sumCredits;

    return Graduate.builder()
        .studentId(student.id())
        .firstName(student.firstName())
        .lastName(student.lastName())
        .path(path)
        .generalAverage(average)
        .results(results)
        .resultYears(resultYears)
        .build();
  }

  private List<StudentInfo> studentsOfPromotion(String promotionId, String path) {
    List<Group> allGroups = groupRepository.findByPromotionId(promotionId);

    List<Group> groups;
    if (path != null) {
      Path pathEnum = Path.valueOf(path);
      groups = allGroups.stream().filter(g -> g.getPath() == pathEnum).toList();
    } else {
      groups = allGroups;
    }

    Map<String, List<GroupFlow>> flowsByStudent = new HashMap<>();
    for (Group group : groups) {
      List<GroupFlow> flows =
          groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc(group.getId());
      for (GroupFlow flow : flows) {
        String studentId = flow.getStudent().getId();
        flowsByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(flow);
      }
    }

    List<StudentInfo> students = new ArrayList<>();
    for (var entry : flowsByStudent.entrySet()) {
      String studentId = entry.getKey();
      List<GroupFlow> flows = entry.getValue();

      GroupFlow latestFlow =
          flows.stream().max(Comparator.comparing(GroupFlow::getFlowDatetime)).orElse(null);

      if (latestFlow != null && latestFlow.getFlowType() == FlowType.JOIN) {
        User student = latestFlow.getStudent();
        students.add(new StudentInfo(studentId, student.getFirstName(), student.getLastName()));
      }
    }

    return students;
  }

  private List<Integer> schoolYearsOf(Promotion promotion) {
    Integer entryYear = promotion.getEntryYear();
    if (entryYear == null) {
      return List.of();
    }
    List<Integer> years = new ArrayList<>();
    for (int i = 0; i < StudentCurriculumService.CURRICULUM_LENGTH_IN_YEARS; i++) {
      years.add(entryYear + i);
    }
    return years;
  }
}
