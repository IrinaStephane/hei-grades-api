package school.hei.api.endpoint.event.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class TranscriptRequestedTest {

  private final TranscriptRequested subject = new TranscriptRequested();

  @Test
  void max_consumer_duration_is_30s() {
    assertEquals(Duration.ofSeconds(30), subject.maxConsumerDuration());
  }

  @Test
  void max_consumer_backoff_is_30s() {
    assertEquals(Duration.ofSeconds(30), subject.maxConsumerBackoffBetweenRetries());
  }
}
