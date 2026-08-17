package school.hei.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class GroupCreation {
  @NotBlank private String ref;

  @NotNull private Path path;

  @NotBlank private String promotionId;
}
