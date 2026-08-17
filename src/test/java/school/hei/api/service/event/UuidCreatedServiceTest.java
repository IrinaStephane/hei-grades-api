package school.hei.api.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.model.UuidCreated;
import school.hei.api.repository.DummyUuidRepository;
import school.hei.api.repository.model.DummyUuid;

class UuidCreatedServiceTest {

  private final DummyUuidRepository dummyUuidRepository = mock(DummyUuidRepository.class);
  private final UuidCreatedService subject = new UuidCreatedService(dummyUuidRepository);

  @Test
  void accept_saves_the_uuid() {
    subject.accept(UuidCreated.builder().uuid("uuid-1").build());

    verify(dummyUuidRepository)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                (DummyUuid dummyUuid) -> "uuid-1".equals(dummyUuid.getId())));
  }
}