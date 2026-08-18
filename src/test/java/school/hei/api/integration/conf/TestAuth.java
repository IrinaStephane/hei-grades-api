package school.hei.api.integration.conf;

import static java.util.UUID.randomUUID;

import school.hei.api.endpoint.rest.security.JwtService;
import school.hei.api.model.User;

/** Mints bearer tokens for users a test owns. */
public class TestAuth {

  public static String tokenFor(JwtService jwtService, User user) {
    return "Bearer " + jwtService.generateToken(user);
  }

  public static String aRandomBearerToken() {
    return "Bearer " + randomUUID();
  }
}
