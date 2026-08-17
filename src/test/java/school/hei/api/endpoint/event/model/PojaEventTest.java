package school.hei.api.endpoint.event.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.EventStack;

class PojaEventTest {

  @Test
  void uuid_created_exposes_durations_and_stack() {
    var subject = UuidCreated.builder().uuid("uuid-1").build();

    assertEquals(Duration.ofSeconds(10), subject.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), subject.maxConsumerBackoffBetweenRetries());
    assertEquals(Duration.ofSeconds(90), subject.eventHandlerInitMaxDuration());
    assertEquals("uuid-1", subject.getUuid());
    assertSame(EventStack.EVENT_STACK_1, subject.getEventStack());
    assertEquals("school.hei.api.event1", subject.getEventSource());
  }

  @Test
  void random_visibility_timeout_is_at_least_init_plus_max_consumer_duration() {
    var subject = UuidCreated.builder().uuid("uuid-1").build();

    assertTrue(subject.randomVisibilityTimeout().toSeconds() >= 100);
  }

  @Test
  void attempt_nb_is_settable() {
    var subject = UuidCreated.builder().uuid("uuid-1").build();
    subject.setAttemptNb(7);

    assertEquals(7, subject.getAttemptNb());
  }

  @Test
  void durably_fallible_exposes_durations() {
    var subject =
        DurablyFallibleUuidCreated1.builder()
            .uuidCreated(UuidCreated.builder().uuid("uuid-1").build())
            .waitDurationBeforeConsumingInSeconds(5)
            .failureRate(0)
            .build();

    assertEquals(Duration.ofSeconds(15), subject.maxConsumerDuration());
    assertEquals(Duration.ofSeconds(30), subject.maxConsumerBackoffBetweenRetries());
  }

  @Test
  void durably_fallible_should_fail_according_to_failure_rate() {
    var alwaysFail =
        DurablyFallibleUuidCreated1.builder()
            .uuidCreated(UuidCreated.builder().uuid("uuid-1").build())
            .waitDurationBeforeConsumingInSeconds(0)
            .failureRate(1)
            .build();
    var neverFail =
        DurablyFallibleUuidCreated1.builder()
            .uuidCreated(UuidCreated.builder().uuid("uuid-1").build())
            .waitDurationBeforeConsumingInSeconds(0)
            .failureRate(0)
            .build();

    assertTrue(alwaysFail.shouldFail());
    assertTrue(!neverFail.shouldFail());
  }
}
