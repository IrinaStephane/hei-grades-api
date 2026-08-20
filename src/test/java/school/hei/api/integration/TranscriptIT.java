package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

class TranscriptIT extends FacadeITMockedThirdParties {

  private User admin;
  private User student;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "transcript-admin-" + randomUUID() + "@hei.school");
    student = saveUser(Role.STUDENT, "transcript-student-" + randomUUID() + "@hei.school");
    when(eventBridgeClientMock.putEvents(any(PutEventsRequest.class)))
        .thenReturn(PutEventsResponse.builder().build());
  }

  @Test
  void admin_requests_transcript_accepted() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId() + "/transcript"),
            HttpMethod.POST,
            entity(admin),
            Object.class);

    assertStatus(HttpStatus.ACCEPTED, response);
  }

  @Test
  void student_requests_own_transcript_accepted() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId() + "/transcript"),
            HttpMethod.POST,
            entity(student),
            Object.class);

    assertStatus(HttpStatus.ACCEPTED, response);
  }

  @Test
  void requests_transcript_for_unknown_user_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + NOT_EXISTING_ID + "/transcript"),
            HttpMethod.POST,
            entity(admin),
            Object.class);

    assertStatus(HttpStatus.NOT_FOUND, response);
  }

  @Test
  void requests_transcript_without_token_is_forbidden() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId() + "/transcript"),
            HttpMethod.POST,
            null,
            Object.class);

    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @AfterEach
  void tearDown() {
    userRepository.delete(student);
    userRepository.delete(admin);
  }
}
