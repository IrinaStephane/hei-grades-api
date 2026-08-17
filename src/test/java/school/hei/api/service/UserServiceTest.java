package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserCreation;
import school.hei.api.model.dto.UserUpdate;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.UserRepository;

class UserServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final UserService subject = new UserService(userRepository, passwordEncoder);

  private static User aUser() {
    return User.builder()
        .id("id-1")
        .firstName("First")
        .lastName("Last")
        .email("user@hei.school")
        .passwordHash("hash")
        .role(Role.STUDENT)
        .createdAt(Instant.now())
        .build();
  }

  @Test
  void get_all_returns_all_when_no_role_filter() {
    var users = List.of(aUser());
    when(userRepository.findAll()).thenReturn(users);

    assertEquals(users, subject.getAll(null));
  }

  @Test
  void get_all_filters_by_role() {
    var users = List.of(aUser());
    when(userRepository.findByRole(Role.STUDENT)).thenReturn(users);

    assertEquals(users, subject.getAll(Role.STUDENT));
  }

  @Test
  void get_by_id_ok() {
    when(userRepository.findById("id-1")).thenReturn(Optional.of(aUser()));

    assertEquals("id-1", subject.getById("id-1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(userRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_user_ok() {
    var creation =
        UserCreation.builder()
            .firstName("First")
            .lastName("Last")
            .email("new@hei.school")
            .password("password123")
            .role(Role.TEACHER)
            .build();
    when(userRepository.existsByEmail("new@hei.school")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded");
    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create(creation);

    assertEquals("new@hei.school", created.getEmail());
    assertEquals("encoded", created.getPasswordHash());
    assertEquals(Role.TEACHER, created.getRole());
  }

  @Test
  void create_user_with_taken_email_conflicts() {
    var creation =
        UserCreation.builder()
            .firstName("First")
            .lastName("Last")
            .email("user@hei.school")
            .password("password123")
            .role(Role.STUDENT)
            .build();
    when(userRepository.existsByEmail("user@hei.school")).thenReturn(true);

    assertThrows(ConflictException.class, () -> subject.create(creation));
    verify(userRepository, never()).save(any());
  }

  @Test
  void update_user_ok() {
    var user = aUser();
    when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated =
        subject.update(
            "id-1", UserUpdate.builder().firstName("New").email("new@hei.school").build());

    assertEquals("New", updated.getFirstName());
    assertEquals("new@hei.school", updated.getEmail());
    verify(userRepository).existsByEmail("new@hei.school");
  }

  @Test
  void update_user_with_taken_email_conflicts() {
    var user = aUser();
    when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
    when(userRepository.existsByEmail("taken@hei.school")).thenReturn(true);

    assertThrows(
        ConflictException.class,
        () -> subject.update("id-1", UserUpdate.builder().email("taken@hei.school").build()));
  }

  @Test
  void update_user_with_same_email_keeps_it() {
    var user = aUser();
    when(userRepository.findById("id-1")).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("id-1", UserUpdate.builder().email("user@hei.school").build());

    assertEquals("user@hei.school", updated.getEmail());
    verify(userRepository, never()).existsByEmail(any());
  }

  @Test
  void delete_user_ok() {
    when(userRepository.existsById("id-1")).thenReturn(true);

    subject.delete("id-1");

    verify(userRepository).deleteById("id-1");
  }

  @Test
  void delete_unknown_user_not_found() {
    when(userRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
    assertTrue(true);
  }
}