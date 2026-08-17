package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserCreation;
import school.hei.api.model.dto.UserRest;
import school.hei.api.model.dto.UserUpdate;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ApiException;
import school.hei.api.model.exception.ApiExceptionType;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public List<UserRest> getAll(Role role) {
    List<User> users = role != null ? userRepository.findByRole(role) : userRepository.findAll();
    return users.stream().map(this::toRest).toList();
  }

  public UserRest getById(String id) {
    return toRest(getEntityById(id));
  }

  public User getEntityById(String id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> new ApiException(ApiExceptionType.NOT_FOUND, "User " + id + " not found"));
  }

  @Transactional
  public UserRest create(UserCreation creation) {
    if (userRepository.existsByEmail(creation.getEmail())) {
      throw new ApiException(
          ApiExceptionType.CONFLICT, "Email " + creation.getEmail() + " is already in use");
    }
    User user =
        User.builder()
            .firstName(creation.getFirstName())
            .lastName(creation.getLastName())
            .email(creation.getEmail())
            .passwordHash(passwordEncoder.encode(creation.getPassword()))
            .role(creation.getRole())
            .createdAt(Instant.now())
            .build();
    return toRest(userRepository.save(user));
  }

  @Transactional
  public UserRest update(String id, UserUpdate update) {
    User user = getEntityById(id);
    if (update.getEmail() != null && !update.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(update.getEmail())) {
        throw new ApiException(
            ApiExceptionType.CONFLICT, "Email " + update.getEmail() + " is already in use");
      }
      user.setEmail(update.getEmail());
    }
    if (update.getFirstName() != null) {
      user.setFirstName(update.getFirstName());
    }
    if (update.getLastName() != null) {
      user.setLastName(update.getLastName());
    }
    if (update.getRole() != null) {
      user.setRole(update.getRole());
    }
    return toRest(userRepository.save(user));
  }

  @Transactional
  public void delete(String id) {
    if (!userRepository.existsById(id)) {
      throw new ApiException(ApiExceptionType.NOT_FOUND, "User " + id + " not found");
    }
    userRepository.deleteById(id);
  }

  private UserRest toRest(User user) {
    return UserRest.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
