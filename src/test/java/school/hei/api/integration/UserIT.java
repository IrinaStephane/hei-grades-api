package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.api.integration.conf.ApiAssertions.assertRestException;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.ApiAssertions.assertValidUUID;
import static school.hei.api.integration.conf.TestAuth.tokenFor;
import static school.hei.api.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.api.endpoint.rest.model.RestException;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.User;
import school.hei.api.model.dto.UserCreation;
import school.hei.api.model.dto.UserRest;
import school.hei.api.model.dto.UserUpdate;
import school.hei.api.model.enums.Role;

class UserIT extends FacadeITMockedThirdParties {

  private User admin;
  private User student;
  private String adminToken;
  private String studentToken;
  private final String userEmail = "student-" + randomUUID() + "@hei.school";

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "admin-" + randomUUID() + "@hei.school");
    student = saveUser(Role.STUDENT, "existing-" + randomUUID() + "@hei.school");
    adminToken = tokenFor(jwtService, admin);
    studentToken = tokenFor(jwtService, student);
  }

  @Test
  void admin_creates_user_ok() {
    var response = createUser(adminToken, aUserCreation());

    assertStatus(HttpStatus.CREATED, response);
    var created = response.getBody();
    assertValidUUID(created.getId());
    assertEquals(userEmail, created.getEmail());
    assertEquals(Role.STUDENT, created.getRole());
  }

  @Test
  void admin_creates_duplicate_email_conflicts() {
    createUser(adminToken, aUserCreation());

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users"),
            HttpMethod.POST,
            entity(adminToken, aUserCreation()),
            RestException.class);
    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT, "Email " + userEmail + " is already in use", response.getBody());
  }

  @Test
  void non_admin_cannot_create_user() {
    var response = createUser(studentToken, aUserCreation());
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_reads_users_ok() {
    var response = getUsers(adminToken, null);
    assertStatus(HttpStatus.OK, response);
    assertTrue(response.getBody().stream().anyMatch(u -> u.getId().equals(student.getId())));
  }

  @Test
  void admin_reads_users_filtered_by_role_ok() {
    var response = getUsers(adminToken, "STUDENT");
    assertStatus(HttpStatus.OK, response);
    assertTrue(response.getBody().stream().allMatch(u -> u.getRole() == Role.STUDENT));
  }

  @Test
  void admin_reads_users_with_bad_role_is_bad_request() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users?role=BOGUS"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, response);
  }

  @Test
  void admin_reads_user_by_id_ok() {
    var response = getUserById(adminToken, student.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(student.getId(), response.getBody().getId());
  }

  @Test
  void admin_reads_unknown_user_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + NOT_EXISTING_ID),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "User " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void user_reads_himself_ok() {
    var response = getUserById(studentToken, student.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(student.getId(), response.getBody().getId());
  }

  @Test
  void user_cannot_read_another_user() {
    var response = getUserById(studentToken, admin.getId());
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_updates_user_ok() {
    var update =
        UserUpdate.builder().firstName("New").lastName("Name").role(Role.TEACHER).build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            UserRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals("New", response.getBody().getFirstName());
    assertEquals(Role.TEACHER, response.getBody().getRole());
  }

  @Test
  void admin_updates_user_with_taken_email_conflicts() {
    var update = UserUpdate.builder().email(admin.getEmail()).build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            RestException.class);

    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT, "Email " + admin.getEmail() + " is already in use", response.getBody());
  }

  @Test
  void admin_deletes_user_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId()),
            HttpMethod.DELETE,
            entity(admin),
            Object.class);
    assertStatus(HttpStatus.NO_CONTENT, response);
  }

  @Test
  void admin_deletes_unknown_user_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + NOT_EXISTING_ID),
            HttpMethod.DELETE,
            entity(admin),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "User " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  private UserCreation aUserCreation() {
    return UserCreation.builder()
        .firstName("Student")
        .lastName("Test")
        .email(userEmail)
        .password("password123")
        .role(Role.STUDENT)
        .build();
  }

  private ResponseEntity<UserRest> createUser(String token, UserCreation creation) {
    return restTemplate.exchange(
        apiUrl(localPort, "/users"),
        HttpMethod.POST,
        entity(token, creation),
        UserRest.class);
  }

  private ResponseEntity<List<UserRest>> getUsers(String token, String role) {
    var url = role == null ? apiUrl(localPort, "/users") : apiUrl(localPort, "/users?role=" + role);
    return restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        new ParameterizedTypeReference<List<UserRest>>() {});
  }

  private ResponseEntity<UserRest> getUserById(String token, String id) {
    return restTemplate.exchange(
        apiUrl(localPort, "/users/" + id),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        UserRest.class);
  }

  @AfterEach
  void tearDown() {
    if (userRepository.existsById(student.getId())) {
      userRepository.delete(student);
    }
    userRepository.delete(admin);
  }
}
