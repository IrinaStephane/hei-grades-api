package school.hei.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CourseAssignmentCreation {
  @NotBlank private String courseId;

  @NotBlank private String teacherId;

  @NotBlank private String groupId;

  @NotNull private Integer year;

  @NotNull private Integer semester;
}
