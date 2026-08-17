package school.hei.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.api.integration.conf.ApiAssertions.assertRestException;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import school.hei.api.endpoint.rest.model.LoginCredentials;
import school.hei.api.endpoint.rest.model.LoginToken;
import school.hei.api.endpoint.rest.model.RestException;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserRest;
import school.hei.api.model.enums.Role;

class AuthIT extends FacadeITMockedThirdParties {

  private User admin;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "auth-admin@hei.school");
  }

  @Test
  void login_ok_returns_token() {
    var response = login("auth-admin@hei.school", "password123");

    assertStatus(HttpStatus.OK, response);
    var token = response.getBody();
    assertEquals(admin.getId(), jwtService.extractUserId(token.getAccessToken()));
    assertEquals(Role.ADMIN, jwtService.extractRole(token.getAccessToken()));
    assertEquals("Bearer", token.getTokenType());
    assertTrue(jwtService.isValid(token.getAccessToken()));
  }

  @Test
  void login_wrong_password_is_unauthorized() {
    var response = login("auth-admin@hei.school", "wrong-password", RestException.class);
    assertStatus(HttpStatus.UNAUTHORIZED, response);
    assertRestException(HttpStatus.UNAUTHORIZED, "Invalid email or password", response.getBody());
  }

  @Test
  void login_unknown_email_is_unauthorized() {
    var response = login("unknown@hei.school", "password123", RestException.class);
    assertStatus(HttpStatus.UNAUTHORIZED, response);
    assertRestException(HttpStatus.UNAUTHORIZED, "Invalid email or password", response.getBody());
  }

  @Test
  void login_invalid_body_is_bad_request() {
    var response =
        restTemplate.postForEntity(
            apiUrl(localPort, "/auth/login"),
            new HttpEntity<>(new LoginCredentials(), jsonHeaders()),
            RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, response);
  }

  @Test
  void me_returns_authenticated_user() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/auth/me"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            UserRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(admin.getId(), response.getBody().getId());
    assertEquals("auth-admin@hei.school", response.getBody().getEmail());
  }

  @Test
  void me_without_token_is_forbidden() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/auth/me"), HttpMethod.GET, new HttpEntity<>(null), Object.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  private ResponseEntity<LoginToken> login(String email, String password) {
    return login(email, password, LoginToken.class);
  }

  private <T> ResponseEntity<T> login(String email, String password, Class<T> responseType) {
    return restTemplate.exchange(
        apiUrl(localPort, "/auth/login"),
        HttpMethod.POST,
        new HttpEntity<>(new LoginCredentials(email, password), jsonHeaders()),
        responseType);
  }

  private static org.springframework.http.HttpHeaders jsonHeaders() {
    var headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @AfterEach
  void tearDown() {
    userRepository.delete(admin);
  }
}
