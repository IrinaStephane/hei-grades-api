package school.hei.api.endpoint.event.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import school.hei.api.endpoint.event.consumer.model.TypedEvent;
import school.hei.api.endpoint.event.model.UuidCreated;
import school.hei.api.service.event.UuidCreatedService;

class EventServiceInvokerTest {

  private final ApplicationContext applicationContext = mock(ApplicationContext.class);
  private final EventServiceInvoker subject = new EventServiceInvoker(applicationContext);

  @Test
  void accept_invokes_the_matching_service_with_the_payload() {
    var event = UuidCreated.builder().uuid("uuid-1").build();
    var typedEvent = new TypedEvent(event.getClass().getTypeName(), event);
    UuidCreatedService service = mock(UuidCreatedService.class);
    when(applicationContext.getBean(UuidCreatedService.class)).thenReturn(service);

    subject.accept(typedEvent);

    verify(service).accept(event);
  }

  @Test
  void accept_throws_for_unknown_event_type() {
    var typedEvent = new TypedEvent("school.hei.api.endpoint.event.model.DoesNotExist", null);

    assertThrows(RuntimeException.class, () -> subject.accept(typedEvent));
  }
}
