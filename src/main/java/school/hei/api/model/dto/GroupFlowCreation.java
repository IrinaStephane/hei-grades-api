package school.hei.api.model.dto;

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
  private String studentId;
  private FlowType flowType;
  private Instant flowDatetime;
}
