package school.hei.api.integration.conf;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@TestConfiguration
public class JdkRestTemplateConf {

  @Bean
  public TestRestTemplate testRestTemplate() {
    // JdkClientHttpRequestFactory, not the JDK HttpURLConnection-based one: the latter throws
    // HttpRetryException on POST requests answered with 401
    return new TestRestTemplate(
        new RestTemplateBuilder().requestFactory(() -> new JdkClientHttpRequestFactory()));
  }
}
