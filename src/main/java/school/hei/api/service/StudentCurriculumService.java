package school.hei.api.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.GroupFlowRepository;

@Service
@AllArgsConstructor
public class StudentCurriculumService {

  public static final int CURRICULUM_LENGTH_IN_YEARS = 3;

  private final GroupFlowRepository groupFlowRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

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

  public Optional<Group> groupForYear(String studentId, int year) {
    return groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId).stream()
        .filter(f -> f.getFlowType() == FlowType.JOIN)
        .filter(f -> f.getFlowDatetime().isBefore(schoolYearStart(year + 1)))
        .max(Comparator.comparing(GroupFlow::getFlowDatetime))
        .map(GroupFlow::getGroup);
  }

  public List<CourseAssignment> assignmentsForYear(String studentId, int year, Path path) {
    var group = groupForYear(studentId, year).orElse(null);
    if (group == null) {
      return List.of();
    }
    return courseAssignmentRepository.findByGroupId(group.getId()).stream()
        .filter(a -> a.getYear() != null && a.getYear() == year)
        .filter(a -> path == null || a.getGroup().getPath() == path)
        .toList();
  }

  public List<CourseAssignment> allAssignments(String studentId, Path path) {
    return schoolYearsOf(studentId).stream()
        .flatMap(year -> assignmentsForYear(studentId, year, path).stream())
        .distinct()
        .toList();
  }

  public static Instant schoolYearStart(int year) {
    return LocalDateTime.of(year, 9, 1, 0, 0).toInstant(ZoneOffset.UTC);
  }
}
