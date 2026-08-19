package school.hei.api.service.event;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.api.endpoint.event.model.TranscriptRequested;
import school.hei.api.export.PdfTranscriptGenerator;
import school.hei.api.file.bucket.BucketComponent;
import school.hei.api.mail.Email;
import school.hei.api.mail.Mailer;
import school.hei.api.repository.UserRepository;

@Service
@AllArgsConstructor
@Slf4j
public class TranscriptRequestedService implements Consumer<TranscriptRequested> {

  private final UserRepository userRepository;
  private final PdfTranscriptGenerator pdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(TranscriptRequested event) {
    var student = userRepository.findById(event.getStudentId()).orElse(null);
    if (student == null) {
      log.error("Transcript requested for unknown student id={}", event.getStudentId());
      return;
    }

    File pdf = pdfGenerator.generate(student, event.getYear());
    try {
      String bucketKey = "transcripts/" + student.getId() + "/" + pdf.getName();
      bucketComponent.upload(pdf, bucketKey);

      var downloadUrl = bucketComponent.presign(bucketKey, Duration.ofDays(7));

      mailer.accept(
          new Email(
              new InternetAddress(student.getEmail()),
              List.of(),
              List.of(),
              "Votre relevé de notes",
              "<p>Bonjour "
                  + student.getFirstName()
                  + ",</p>"
                  + "<p>Votre relevé de notes est prêt : <a href=\""
                  + downloadUrl
                  + "\">télécharger</a></p>",
              List.of()));
    } finally {
      if (pdf.exists() && !pdf.delete()) {
        log.warn("Failed to delete temporary transcript PDF: {}", pdf.getAbsolutePath());
      }
    }
  }
}
