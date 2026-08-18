package school.hei.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.api.model.enums.FlowType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupFlowCreation {
  @NotBlank private String studentId;

  @NotNull private FlowType flowType;

  private Instant flowDatetime;
}
