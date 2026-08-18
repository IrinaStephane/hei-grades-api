package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.api.endpoint.rest.model.LoginCredentials;
import school.hei.api.endpoint.rest.security.JwtService;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.UnauthorizedException;
import school.hei.api.repository.UserRepository;

class AuthServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final JwtService jwtService = mock(JwtService.class);
  private final AuthService subject = new AuthService(userRepository, passwordEncoder, jwtService);

  private static User aUser() {
    return User.builder()
        .id("id-1")
        .firstName("First")
        .lastName("Last")
        .email("user@hei.school")
        .passwordHash("encoded-password")
        .role(Role.STUDENT)
        .build();
  }

  @Test
  void login_ok_returns_token() {
    var user = aUser();
    when(userRepository.findByEmail("user@hei.school")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("access-token");
    when(jwtService.getExpirationSeconds()).thenReturn(3600L);

    var token = subject.login(new LoginCredentials("user@hei.school", "password123"));

    assertEquals("access-token", token.getAccessToken());
    assertEquals("Bearer", token.getTokenType());
    assertEquals(3600L, token.getExpiresIn());
  }

  @Test
  void login_with_unknown_email_is_unauthorized() {
    when(userRepository.findByEmail("unknown@hei.school")).thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedException.class,
        () -> subject.login(new LoginCredentials("unknown@hei.school", "password123")));
  }

  @Test
  void login_with_wrong_password_is_unauthorized() {
    var user = aUser();
    when(userRepository.findByEmail("user@hei.school")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

    assertThrows(
        UnauthorizedException.class,
        () -> subject.login(new LoginCredentials("user@hei.school", "wrong")));
  }
}
