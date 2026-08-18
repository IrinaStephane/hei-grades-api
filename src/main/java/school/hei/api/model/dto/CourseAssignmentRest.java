package school.hei.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAssignmentRest {
  private String id;
  private String courseId;
  private String teacherId;
  private String groupId;
  private Integer year;
  private Integer semester;
}
