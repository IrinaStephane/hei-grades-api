package school.hei.api.endpoint.rest.security;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;

class JwtServiceTest {

  private final JwtService subject = new JwtService("test-secret-key-for-hei-grades-api-32chars", 3600);

  private static User aUser() {
    return User.builder()
        .id("user-id-1")
        .firstName("First")
        .lastName("Last")
        .email("user@hei.school")
        .passwordHash("hash")
        .role(Role.TEACHER)
        .createdAt(now())
        .build();
  }

  @Test
  void generate_token_contains_user_identity() {
    var token = subject.generateToken(aUser());

    assertEquals("user-id-1", subject.extractUserId(token));
    assertEquals(Role.TEACHER, subject.extractRole(token));
    assertTrue(subject.isValid(token));
  }

  @Test
  void expiration_seconds_are_those_configured() {
    assertEquals(3600, subject.getExpirationSeconds());
  }

  @Test
  void garbage_token_is_not_valid() {
    assertFalse(subject.isValid("not-a-jwt"));
  }

  @Test
  void token_with_another_secret_is_not_valid() {
    var otherService = new JwtService("another-secret-key-for-hei-grades-api-32chars", 3600);
    var token = otherService.generateToken(aUser());

    assertFalse(subject.isValid(token));
  }
}
