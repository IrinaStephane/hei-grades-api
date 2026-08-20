package school.hei.api.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserCreation;
import school.hei.api.model.dto.UserUpdate;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public List<User> getAll(Role role) {
    return role != null ? userRepository.findByRole(role) : userRepository.findAll();
  }

  public User getById(String id) {
    return getEntityById(id);
  }

  public User getEntityById(String id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
  }

  @Transactional
  public User create(UserCreation creation) {
    if (userRepository.existsByEmail(creation.getEmail())) {
      throw new ConflictException("Email " + creation.getEmail() + " is already in use");
    }
    Instant createdAt = Instant.now();
    User user =
        User.builder()
            .firstName(creation.getFirstName())
            .lastName(creation.getLastName())
            .email(creation.getEmail())
            .passwordHash(passwordEncoder.encode(creation.getPassword()))
            .role(creation.getRole())
            .matricule(generateMatricule(creation.getRole(), createdAt))
            .createdAt(createdAt)
            .build();
    return userRepository.save(user);
  }

  private String generateMatricule(Role role, Instant createdAt) {
    if (role == Role.STUDENT) {
      String prefix =
          "STD" + DateTimeFormatter.ofPattern("yy").format(createdAt.atZone(ZoneOffset.UTC));
      return prefix + String.format("%03d", nextSequence(prefix, 3));
    }
    if (role == Role.TEACHER) {
      return "TEACH" + String.format("%02d", nextSequence("TEACH", 2));
    }
    return "ADMIN" + String.format("%02d", nextSequence("ADMIN", 2));
  }

  private int nextSequence(String prefix, int width) {
    return userRepository.findAll().stream()
            .map(User::getMatricule)
            .filter(m -> m != null && m.startsWith(prefix))
            .mapToInt(m -> Integer.parseInt(m.substring(m.length() - width)))
            .max()
            .orElse(0)
        + 1;
  }

  @Transactional
  public User update(String id, UserUpdate update) {
    User user = getEntityById(id);
    if (update.getEmail() != null && !update.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(update.getEmail())) {
        throw new ConflictException("Email " + update.getEmail() + " is already in use");
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
    return userRepository.save(user);
  }

  @Transactional
  public void delete(String id) {
    if (!userRepository.existsById(id)) {
      throw new NotFoundException("User " + id + " not found");
    }
    userRepository.deleteById(id);
  }
}
