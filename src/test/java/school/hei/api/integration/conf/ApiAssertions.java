package school.hei.api.integration.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.api.endpoint.rest.model.RestException;

/** Assertion helpers shared by the integration tests. */
public class ApiAssertions {

  public static void assertStatus(HttpStatus expected, ResponseEntity<?> response) {
    assertEquals(expected, response.getStatusCode());
  }

  public static void assertRestException(HttpStatus status, String message, RestException body) {
    assertEquals(status.toString(), body.getType());
    assertEquals(message, body.getMessage());
  }

  public static void assertValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
    } catch (Exception e) {
      fail("Not a valid uuid: " + candidate);
    }
  }
}
