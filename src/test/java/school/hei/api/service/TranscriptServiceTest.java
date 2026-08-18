package school.hei.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.EventProducer;
import school.hei.api.endpoint.event.model.TranscriptRequested;

class TranscriptServiceTest {

  private final EventProducer<TranscriptRequested> eventProducer = mock(EventProducer.class);
  private final TranscriptService subject = new TranscriptService(eventProducer);

  @Test
  void request_transcript_sends_event() {
    subject.requestTranscript("student-1", 2025);

    verify(eventProducer).accept(any(List.class));
  }

  @Test
  void request_transcript_with_null_year() {
    subject.requestTranscript("student-1", null);

    verify(eventProducer).accept(any(List.class));
  }
}
