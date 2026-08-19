package school.hei.api.service.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.event.model.TranscriptRequested;
import school.hei.api.export.PdfTranscriptGenerator;
import school.hei.api.file.bucket.BucketComponent;
import school.hei.api.mail.Mailer;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.UserRepository;

class TranscriptRequestedServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final PdfTranscriptGenerator pdfGenerator = mock(PdfTranscriptGenerator.class);
  private final BucketComponent bucketComponent = mock(BucketComponent.class);
  private final Mailer mailer = mock(Mailer.class);
  private final TranscriptRequestedService subject =
      new TranscriptRequestedService(userRepository, pdfGenerator, bucketComponent, mailer);

  private static User aStudent() {
    return User.builder()
        .id("s1")
        .firstName("John")
        .lastName("Doe")
        .email("john@hei.school")
        .passwordHash("hash")
        .role(Role.STUDENT)
        .build();
  }

  @Test
  void accept_generates_pdf_uploads_and_sends_email() {
    var event = TranscriptRequested.builder().studentId("s1").year(2025).build();
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    File pdfFile = mock(File.class);
    when(pdfFile.getName()).thenReturn("transcript.pdf");
    when(pdfGenerator.generate(any(User.class), eq(2025))).thenReturn(pdfFile);
    when(bucketComponent.presign(any(String.class), any(Duration.class)))
        .thenAnswer(inv -> java.net.URI.create("https://example.com/file.pdf").toURL());

    subject.accept(event);

    verify(pdfGenerator).generate(any(User.class), eq(2025));
    verify(bucketComponent).upload(pdfFile, "transcripts/s1/transcript.pdf");
    verify(mailer).accept(any());
  }

  @Test
  void accept_unknown_student_logs_and_returns_early() {
    var event = TranscriptRequested.builder().studentId("unknown").year(2025).build();
    when(userRepository.findById("unknown")).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> subject.accept(event));

    verify(pdfGenerator, never()).generate(any(), any());
    verify(bucketComponent, never()).upload(any(), any());
    verify(mailer, never()).accept(any());
  }
}
