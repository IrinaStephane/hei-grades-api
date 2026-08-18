package school.hei.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.api.model.enums.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRest {
  private String id;
  private String ref;
  private Path path;
  private String promotionId;
}
