package school.hei.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestAuth.tokenFor;
import static school.hei.api.integration.conf.TestMocks.setUpBucket;
import static school.hei.api.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.api.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.api.endpoint.rest.model.Whoami;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;

class HealthControllerIT extends FacadeITMockedThirdParties {

  private User admin;
  private String adminToken;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "health-admin@hei.school");
    adminToken = tokenFor(jwtService, admin);
    setUpEventBridge(eventBridgeClientMock);
    setUpBucket(bucketComponent, new AtomicReference<File>());
  }

  @Test
  void ping_is_ok() {
    var response = get("/ping", String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void health_db_is_ok() {
    assertStatus(HttpStatus.OK, get("/health/db", String.class));
  }

  @Test
  void health_bucket_is_ok() {
    assertStatus(HttpStatus.OK, get("/health/bucket", String.class));
  }

  @Test
  void health_email_is_ok() {
    var response = get("/health/email?to=health-check@hei.school", String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void health_email_missing_param_is_bad_request() {
    var response = get("/health/email", Object.class);
    assertStatus(HttpStatus.BAD_REQUEST, response);
  }

  @Test
  void ping_without_token_is_ok() {
    var response =
        restTemplate.exchange(apiUrl(localPort, "/ping"), HttpMethod.GET, null, String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void health_db_without_token_is_ok() {
    var response =
        restTemplate.exchange(apiUrl(localPort, "/health/db"), HttpMethod.GET, null, String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void graduates_page_without_token_is_ok() {
    var response =
        restTemplate.exchange(apiUrl(localPort, "/graduates"), HttpMethod.GET, null, String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void whoami_with_token_returns_id_and_role() {
    var response = get("/whoami", Whoami.class);
    assertStatus(HttpStatus.OK, response);
    assertEquals(admin.getId(), response.getBody().id());
    assertEquals("ADMIN", response.getBody().role());
  }

  @Test
  void whoami_without_token_is_forbidden() {
    var response =
        restTemplate.exchange(apiUrl(localPort, "/whoami"), HttpMethod.GET, null, String.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void protected_route_without_token_is_forbidden() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/promotions/promo-1/graduates"),
            HttpMethod.GET,
            null,
            String.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void health_event1_generates_uuids() {
    var response = get("/health/event1?nbEvent=3&waitInSeconds=0", List.class);
    assertStatus(HttpStatus.OK, response);
    assertEquals(3, response.getBody().size());
  }

  @Test
  void health_event1_ko_on_bad_nb_event() {
    var response = get("/health/event1?nbEvent=600&waitInSeconds=0", Object.class);
    assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, response);
  }

  @Test
  void check_uuids_saved_ok() {
    var response = post("/health/event/uuids", List.of("dummy-uuid-id-1"), String.class);
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void check_uuids_not_saved_ko() {
    var response = post("/health/event/uuids", List.of(NOT_EXISTING_ID), String.class);
    assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, response);
  }

  private <T> ResponseEntity<T> get(String path, Class<T> responseType) {
    return restTemplate.exchange(
        apiUrl(localPort, path),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(admin)),
        responseType);
  }

  private <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
    return restTemplate.exchange(
        apiUrl(localPort, path), HttpMethod.POST, entity(admin, body), responseType);
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() {
    userRepository.delete(admin);
  }
}
