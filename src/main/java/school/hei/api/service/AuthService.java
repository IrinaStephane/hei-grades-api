package school.hei.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.hei.api.config.security.JwtService;
import school.hei.api.endpoint.rest.model.LoginCredentials;
import school.hei.api.endpoint.rest.model.LoginToken;
import school.hei.api.model.User;
import school.hei.api.model.exception.ApiException;
import school.hei.api.model.exception.ApiExceptionType;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginToken login(LoginCredentials credentials) {
    User user =
        userRepository
            .findByEmail(credentials.getEmail())
            .orElseThrow(
                () ->
                    new ApiException(ApiExceptionType.UNAUTHORIZED, "Invalid email or password"));

    if (!passwordEncoder.matches(credentials.getPassword(), user.getPasswordHash())) {
      throw new ApiException(ApiExceptionType.UNAUTHORIZED, "Invalid email or password");
    }

    return LoginToken.builder()
        .accessToken(jwtService.generateToken(user))
        .tokenType("Bearer")
        .expiresIn(jwtService.getExpirationSeconds())
        .build();
  }
}