package school.hei.api.endpoint.event.consumer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.EventConf;
import school.hei.api.endpoint.event.model.UuidCreated;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

class ConsumableEventTyperTest {

  private final ObjectMapper om = new ObjectMapper();
  private final SqsClient sqsClient = mock(SqsClient.class);
  private final EventConf eventConf = mock(EventConf.class);
  private final ConsumableEventTyper subject = new ConsumableEventTyper(om, eventConf);

  private static SQSMessage aMessage(String body) {
    var message = new SQSMessage();
    message.setBody(body);
    message.setReceiptHandle("receipt-handle-1");
    message.setAttributes(Map.of("ApproximateReceiveCount", "3"));
    return message;
  }

  @Test
  void types_a_valid_message() {
    when(eventConf.getSqsClient()).thenReturn(sqsClient);
    var message =
        aMessage(
            """
            {"detail-type": "school.hei.api.endpoint.event.model.UuidCreated", \
            "detail": {"uuid": "uuid-1"}}
            """);

    var consumableEvents = subject.apply(List.of(message));

    assertEquals(1, consumableEvents.size());
    var consumableEvent = consumableEvents.get(0);
    assertEquals(
        "school.hei.api.endpoint.event.model.UuidCreated", consumableEvent.getEvent().typeName());
    var payload = (UuidCreated) consumableEvent.getEvent().payload();
    assertEquals("uuid-1", payload.getUuid());
    assertEquals(3, payload.getAttemptNb());
  }

  @Test
  void ack_deletes_the_message() {
    when(eventConf.getSqsClient()).thenReturn(sqsClient);
    var message =
        aMessage(
            """
            {"detail-type": "school.hei.api.endpoint.event.model.UuidCreated", \
            "detail": {"uuid": "uuid-1"}}
            """);

    var consumableEvent = subject.apply(List.of(message)).get(0);
    consumableEvent.ack();

    verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
  }

  @Test
  void new_random_visibility_timeout_changes_visibility() {
    when(eventConf.getSqsClient()).thenReturn(sqsClient);
    var message =
        aMessage(
            """
            {"detail-type": "school.hei.api.endpoint.event.model.UuidCreated", \
            "detail": {"uuid": "uuid-1"}}
            """);

    var consumableEvent = subject.apply(List.of(message)).get(0);
    consumableEvent.newRandomVisibilityTimeout();

    verify(sqsClient).changeMessageVisibility(any(ChangeMessageVisibilityRequest.class));
  }

  @Test
  void invalid_message_is_skipped() {
    when(eventConf.getSqsClient()).thenReturn(sqsClient);
    var message = aMessage("not-json");

    var consumableEvents = subject.apply(List.of(message));

    assertTrue(consumableEvents.isEmpty());
    verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
  }
}
