package school.hei.api.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.datastructure.ListGrouper;
import school.hei.api.endpoint.event.model.UuidCreated;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

class EventProducerTest {

  private final ObjectMapper om = mock(ObjectMapper.class);
  private final EventBridgeClient eventBridgeClient = mock(EventBridgeClient.class);
  private final ListGrouper<UuidCreated> listGrouper = mock(ListGrouper.class);
  private final EventProducer<UuidCreated> subject =
      new EventProducer<>(om, eventBridgeClient, "my-bus", listGrouper);

  private static UuidCreated aUuidCreated() {
    return UuidCreated.builder().uuid("uuid-1").build();
  }

  @Test
  void accept_sends_events_in_batches() throws Exception {
    var event = aUuidCreated();
    when(listGrouper.apply(any(), anyInt())).thenReturn(List.of(List.of(event)));
    when(om.writeValueAsString(event)).thenReturn("{\"uuid\":\"uuid-1\"}");
    when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
        .thenReturn(
            PutEventsResponse.builder()
                .entries(
                    List.of(
                        PutEventsResultEntry.builder()
                            .eventId("event-id-1")
                            .build()))
                .build());

    subject.accept(List.of(event));

    verify(eventBridgeClient)
        .putEvents(
            org.mockito.ArgumentMatchers.argThat(
                (PutEventsRequest request) ->
                    request.entries().size() == 1
                        && "my-bus".equals(request.entries().get(0).eventBusName())
                        && "school.hei.api.event1".equals(request.entries().get(0).source())
                        && "school.hei.api.endpoint.event.model.UuidCreated"
                            .equals(request.entries().get(0).detailType())));
  }

  @Test
  void accept_splits_more_than_ten_events_into_batches() throws Exception {
    var event = aUuidCreated();
    when(listGrouper.apply(any(), anyInt()))
        .thenReturn(List.of(List.of(event), List.of(event, event)));
    when(om.writeValueAsString(any())).thenReturn("{}");
    when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
        .thenReturn(PutEventsResponse.builder().entries(List.of()).build());

    subject.accept(List.of(event, event, event));

    verify(eventBridgeClient, times(2)).putEvents(any(PutEventsRequest.class));
  }

  @Test
  void accept_throws_when_batch_exceeds_max_entries() {
    var event = aUuidCreated();
    var tooManyEvents = new java.util.ArrayList<UuidCreated>();
    for (int i = 0; i < 11; i++) {
      tooManyEvents.add(event);
    }
    when(listGrouper.apply(any(), anyInt())).thenReturn(List.of(tooManyEvents));

    assertThrows(RuntimeException.class, () -> subject.accept(tooManyEvents));
  }

  @Test
  void accept_propagates_json_processing_failure() throws Exception {
    var event = aUuidCreated();
    when(listGrouper.apply(any(), anyInt())).thenReturn(List.of(List.of(event)));
    when(om.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

    assertThrows(RuntimeException.class, () -> subject.accept(List.of(event)));
  }
}