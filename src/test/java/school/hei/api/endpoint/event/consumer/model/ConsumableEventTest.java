package school.hei.api.endpoint.event.consumer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class ConsumableEventTest {

  @Test
  void ack_runs_the_acknowledger() {
    var event = mock(TypedEvent.class);
    var acknowledger = mock(Runnable.class);
    var visibilitySetter = mock(Runnable.class);
    var subject = new ConsumableEvent(event, acknowledger, visibilitySetter);

    subject.ack();

    verify(acknowledger).run();
  }

  @Test
  void new_random_visibility_timeout_runs_the_setter() {
    var event = mock(TypedEvent.class);
    var acknowledger = mock(Runnable.class);
    var visibilitySetter = mock(Runnable.class);
    var subject = new ConsumableEvent(event, acknowledger, visibilitySetter);

    subject.newRandomVisibilityTimeout();

    verify(visibilitySetter).run();
  }

  @Test
  void exposes_the_typed_event() {
    var event = mock(TypedEvent.class);
    var subject = new ConsumableEvent(event, () -> {}, () -> {});

    assertEquals(event, subject.getEvent());
  }
}
