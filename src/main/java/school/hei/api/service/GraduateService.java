package school.hei.api.service;

import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.UserRepository;

@Service
@AllArgsConstructor
public class GraduateService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseRepository courseRepository;
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
  }

  private record StudentInfo(String id, String firstName, String lastName, List<String> groupIds) {}

  public List<Graduate> getGraduates(String promotionId, String path) {
    var students = studentsOfPromotion(promotionId, path);

    return students.stream()
        .map(student -> toGraduate(student, path))
        .filter(g -> g.getResults().stream().allMatch(r -> r.getFinalGrade() >= 10))
        .sorted(Comparator.comparingDouble(Graduate::getGeneralAverage).reversed())
        .toList();
  }

  private Graduate toGraduate(StudentInfo student, String path) {
    var assignments = courseAssignmentsForGroups(student.groupIds(), path);

    var results =
        assignments.stream()
            .map(
                assignment -> {
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
                  return CourseResult.builder()
                      .courseId(course.getId())
                      .credits(course.getCredits())
                      .finalGrade(finalGrade)
                      .build();
                })
            .toList();

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
        .build();
  }

  private List<StudentInfo> studentsOfPromotion(String promotionId, String path) {
    throw new UnsupportedOperationException(
        "Depends on Group/GroupFlow entities - to implement once available on dev");
  }

  private List<school.hei.api.model.CourseAssignment> courseAssignmentsForGroups(
      List<String> groupIds, String path) {
    throw new UnsupportedOperationException(
        "Depends on CourseAssignment/Group entities - to implement once available on dev");
  }
}
