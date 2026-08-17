package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.mapper.UserMapper;
import school.hei.api.endpoint.rest.model.LoginCredentials;
import school.hei.api.endpoint.rest.model.LoginToken;
import school.hei.api.model.dto.UserRest;
import school.hei.api.service.AuthService;
import school.hei.api.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;
  private final UserService userService;
  private final UserMapper userMapper;

  @PostMapping("/login")
  public LoginToken login(@Valid @RequestBody LoginCredentials credentials) {
    return authService.login(credentials);
  }

  @GetMapping("/me")
  public UserRest getMe(Authentication authentication) {
    // the JwtAuthenticationFilter sets the user id as the authentication
    // principal name, see JwtAuthenticationFilter
    return userMapper.toRest(userService.getById(authentication.getName()));
  }
}