package school.hei.api.endpoint.event.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.concurrency.Workers;
import school.hei.api.endpoint.event.consumer.model.ConsumableEvent;
import school.hei.api.endpoint.event.consumer.model.TypedEvent;
import school.hei.api.endpoint.event.model.UuidCreated;

class EventConsumerTest {

  private final Workers<Void> workers = new Workers<>();
  private final EventServiceInvoker eventServiceInvoker = mock(EventServiceInvoker.class);
  private final EventConsumer subject = new EventConsumer(workers, eventServiceInvoker);

  @Test
  void accept_invokes_service_then_acks_each_event() {
    var event = UuidCreated.builder().uuid("uuid-1").build();
    var typedEvent = new TypedEvent(event.getClass().getTypeName(), event);
    var acknowledger = mock(Runnable.class);
    var consumableEvent = new ConsumableEvent(typedEvent, acknowledger, () -> {});

    subject.accept(List.of(consumableEvent));

    verify(eventServiceInvoker).accept(typedEvent);
    verify(acknowledger).run();
  }
}