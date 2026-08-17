package school.hei.api.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.api.model.enums.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreation {
  @NotBlank private String firstName;

  @NotBlank private String lastName;

  @NotBlank @Email private String email;

  @NotBlank private String password;

  @NotNull private Role role;
}