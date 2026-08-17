package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.dto.CourseAssignmentRest;

@Component
@AllArgsConstructor
public class CourseAssignmentMapper {

  public CourseAssignmentRest toRest(CourseAssignment assignment) {
    return CourseAssignmentRest.builder()
        .id(assignment.getId())
        .courseId(assignment.getCourse().getId())
        .teacherId(assignment.getTeacher().getId())
        .groupId(assignment.getGroup().getId())
        .year(assignment.getYear())
        .semester(assignment.getSemester())
        .build();
  }

  public List<CourseAssignmentRest> toRest(List<CourseAssignment> assignments) {
    return assignments.stream().map(this::toRest).toList();
  }
}
