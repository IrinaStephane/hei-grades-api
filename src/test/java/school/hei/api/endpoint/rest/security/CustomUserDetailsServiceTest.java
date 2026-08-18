package school.hei.api.endpoint.rest.security;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.UserRepository;

class CustomUserDetailsServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final CustomUserDetailsService subject = new CustomUserDetailsService(userRepository);

  @Test
  void loads_user_by_email() {
    var user =
        User.builder()
            .id("id-1")
            .firstName("First")
            .lastName("Last")
            .email("user@hei.school")
            .passwordHash("hash")
            .role(Role.TEACHER)
            .createdAt(now())
            .build();
    when(userRepository.findByEmail("user@hei.school")).thenReturn(Optional.of(user));

    var details = subject.loadUserByUsername("user@hei.school");

    assertEquals("user@hei.school", details.getUsername());
    assertEquals("hash", details.getPassword());
  }

  @Test
  void unknown_email_throws() {
    when(userRepository.findByEmail("unknown@hei.school")).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class, () -> subject.loadUserByUsername("unknown@hei.school"));
  }
}
