package school.hei.api.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("school.hei.api.jwt.secret", () -> "test-secret-key-for-hei-grades-api-32chars");
    registry.add("school.hei.api.jwt.expiration-seconds", () -> "3600");
  }
}
