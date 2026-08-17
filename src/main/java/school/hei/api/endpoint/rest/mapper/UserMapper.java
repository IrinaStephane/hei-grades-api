package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserRest;

@Component
@AllArgsConstructor
public class UserMapper {

  public UserRest toRest(User user) {
    return UserRest.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public List<UserRest> toRest(List<User> users) {
    return users.stream().map(this::toRest).toList();
  }
}
