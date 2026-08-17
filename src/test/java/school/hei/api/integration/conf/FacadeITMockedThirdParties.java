package school.hei.api.integration.conf;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.api.conf.FacadeIT;
import school.hei.api.endpoint.rest.security.JwtService;
import school.hei.api.file.bucket.BucketComponent;
import school.hei.api.file.bucket.BucketConf;
import school.hei.api.mail.Mailer;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@AutoConfigureMockMvc
@Import(JdkRestTemplateConf.class)
public class FacadeITMockedThirdParties extends FacadeIT {
  @LocalServerPort protected int localPort;
  @MockBean protected BucketConf bucketConf;
  @MockBean protected BucketComponent bucketComponent;
  @MockBean protected Mailer mailer;
  @MockBean protected EventBridgeClient eventBridgeClientMock;
  @MockBean protected SesClient sesClientMock;
  @MockBean protected SqsClient sqsClientMock;

  @Autowired protected TestRestTemplate restTemplate;
  @Autowired protected UserRepository userRepository;
  @Autowired protected PasswordEncoder passwordEncoder;
  @Autowired protected JwtService jwtService;

  protected User saveUser(Role role, String email) {
    return userRepository.save(
        User.builder()
            .firstName("First")
            .lastName("Last")
            .email(email)
            .passwordHash(passwordEncoder.encode("password123"))
            .role(role)
            .createdAt(Instant.now())
            .build());
  }

  protected String bearerToken(User user) {
    return "Bearer " + jwtService.generateToken(user);
  }

  protected HttpHeaders authHeaders(User user) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, bearerToken(user));
    return headers;
  }

  protected static HttpHeaders authHeaders(String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
    return headers;
  }

  protected <T> HttpEntity<T> entity(User user, T body) {
    return new HttpEntity<>(body, authHeaders(user));
  }

  protected static <T> HttpEntity<T> entity(String bearerToken, T body) {
    return new HttpEntity<>(body, authHeaders(bearerToken));
  }

  protected HttpEntity<Void> entity(User user) {
    return new HttpEntity<>(authHeaders(user));
  }
}