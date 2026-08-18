package school.hei.api.endpoint.rest.security;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;

class CustomUserDetailsTest {

  @Test
  void exposes_user_information() {
    var user =
        User.builder()
            .id("id-1")
            .firstName("First")
            .lastName("Last")
            .email("user@hei.school")
            .passwordHash("hash")
            .role(Role.STUDENT)
            .createdAt(now())
            .build();

    var subject = new CustomUserDetails(user);

    assertEquals(user, subject.getUser());
    assertEquals("hash", subject.getPassword());
    assertEquals("user@hei.school", subject.getUsername());
    assertEquals(
        List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")),
        subject.getAuthorities());
    assertTrue(subject.isAccountNonExpired());
    assertTrue(subject.isAccountNonLocked());
    assertTrue(subject.isCredentialsNonExpired());
    assertTrue(subject.isEnabled());
  }
}
