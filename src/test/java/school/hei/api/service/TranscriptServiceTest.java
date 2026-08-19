package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.EventProducer;
import school.hei.api.endpoint.event.model.TranscriptRequested;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.UserRepository;

class TranscriptServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final EventProducer<TranscriptRequested> eventProducer = mock(EventProducer.class);
  private final TranscriptService subject = new TranscriptService(userRepository, eventProducer);

  @Test
  void request_transcript_sends_event() {
    when(userRepository.existsById("student-1")).thenReturn(true);

    subject.requestTranscript("student-1", 2025);

    verify(eventProducer).accept(any(List.class));
  }

  @Test
  void request_transcript_with_null_year() {
    when(userRepository.existsById("student-1")).thenReturn(true);

    subject.requestTranscript("student-1", null);

    verify(eventProducer).accept(any(List.class));
  }

  @Test
  void request_transcript_of_unknown_student_not_found() {
    when(userRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.requestTranscript("nope", 2025));
    verify(eventProducer, never()).accept(any());
  }
}
