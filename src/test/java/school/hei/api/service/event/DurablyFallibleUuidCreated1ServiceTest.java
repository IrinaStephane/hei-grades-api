package school.hei.api.service.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.model.DurablyFallibleUuidCreated1;
import school.hei.api.endpoint.event.model.UuidCreated;

class DurablyFallibleUuidCreated1ServiceTest {

  private final UuidCreatedService uuidCreatedService = mock(UuidCreatedService.class);
  private final DurablyFallibleUuidCreated1Service subject =
      new DurablyFallibleUuidCreated1Service(uuidCreatedService);

  private static DurablyFallibleUuidCreated1 anEvent(double failureRate) {
    return DurablyFallibleUuidCreated1.builder()
        .uuidCreated(UuidCreated.builder().uuid("uuid-1").build())
        .waitDurationBeforeConsumingInSeconds(0)
        .failureRate(failureRate)
        .build();
  }

  @Test
  void accept_forwards_to_uuid_created_service_when_no_failure() {
    subject.accept(anEvent(0));

    verify(uuidCreatedService)
        .accept(
            org.mockito.ArgumentMatchers.argThat(
                (UuidCreated event) -> "uuid-1".equals(event.getUuid())));
  }

  @Test
  void accept_throws_when_should_fail() {
    assertThrows(RuntimeException.class, () -> subject.accept(anEvent(1)));

    verify(uuidCreatedService, never()).accept(org.mockito.ArgumentMatchers.any());
  }
}