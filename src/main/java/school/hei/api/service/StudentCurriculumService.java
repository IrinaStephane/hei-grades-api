package school.hei.api.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.enums.FlowType;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;

@Service
@AllArgsConstructor
public class StudentCurriculumService {

  public static final int CURRICULUM_LENGTH_IN_YEARS = 3;

  private final GroupFlowRepository groupFlowRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  public List<Integer> schoolYearsOf(String studentId) {
    var flows = groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId);
    int firstYear =
        flows.stream()
            .filter(f -> f.getFlowType() == FlowType.JOIN)
            .mapToInt(f -> f.getFlowDatetime().atZone(ZoneOffset.UTC).getYear())
            .min()
            .orElse(0);
    int lastYear =
        flows.stream()
            .mapToInt(f -> f.getFlowDatetime().atZone(ZoneOffset.UTC).getYear())
            .max()
            .orElse(firstYear);
    List<Integer> years = new ArrayList<>();
    for (int y = firstYear; y <= lastYear; y++) {
      years.add(y);
    }
    return years;
  }

  public List<Group> groupsForYear(String studentId, int year) {
    var flows = groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId);
    Instant start = schoolYearStart(year);
    Instant end = schoolYearStart(year + 1);
    return flows.stream()
        .filter(f -> f.getFlowType() == FlowType.JOIN)
        .filter(f -> f.getFlowDatetime().isBefore(end))
        .filter(f -> overlapsSchoolYear(flows, f, start))
        .map(GroupFlow::getGroup)
        .distinct()
        .toList();
  }

  private static boolean overlapsSchoolYear(List<GroupFlow> flows, GroupFlow join, Instant start) {
    Instant leave =
        flows.stream()
            .filter(f -> f.getFlowType() == FlowType.LEAVE)
            .filter(f -> f.getGroup().getId().equals(join.getGroup().getId()))
            .filter(f -> f.getFlowDatetime().isAfter(join.getFlowDatetime()))
            .map(GroupFlow::getFlowDatetime)
            .min(Comparator.naturalOrder())
            .orElse(null);
    return leave == null || leave.isAfter(start);
  }

  public List<CourseAssignment> assignmentsForYear(String studentId, int year) {
    List<CourseAssignment> assignments =
        groupsForYear(studentId, year).stream()
            .flatMap(group -> courseAssignmentRepository.findByGroupId(group.getId()).stream())
            .filter(a -> a.getYear() != null && a.getYear() == year)
            .distinct()
            .toList();
    return keepBestAssignmentPerCourse(studentId, assignments);
  }

  private List<CourseAssignment> keepBestAssignmentPerCourse(
      String studentId, List<CourseAssignment> assignments) {
    Map<String, CourseAssignment> bestByCourse = new LinkedHashMap<>();
    for (CourseAssignment assignment : assignments) {
      bestByCourse.merge(
          assignment.getCourse().getId(),
          assignment,
          (current, candidate) -> hasGrade(studentId, current) ? current : candidate);
    }
    return new ArrayList<>(bestByCourse.values());
  }

  private boolean hasGrade(String studentId, CourseAssignment assignment) {
    return examRepository.findByCourseAssignmentId(assignment.getId()).stream()
        .anyMatch(
            exam ->
                gradeRepository.findByExamId(exam.getId()).stream()
                    .anyMatch(g -> g.getStudentId().equals(studentId)));
  }

  public List<CourseAssignment> allAssignments(String studentId) {
    return schoolYearsOf(studentId).stream()
        .flatMap(year -> assignmentsForYear(studentId, year).stream())
        .distinct()
        .toList();
  }

  public static Instant schoolYearStart(int year) {
    return LocalDateTime.of(year, 9, 1, 0, 0).toInstant(ZoneOffset.UTC);
  }
}
