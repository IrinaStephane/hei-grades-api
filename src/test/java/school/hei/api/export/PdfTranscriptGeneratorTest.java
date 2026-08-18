package school.hei.api.export;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;

class PdfTranscriptGeneratorTest {

  private final PdfTranscriptGenerator subject = new PdfTranscriptGenerator();

  private static User aStudent() {
    return User.builder()
        .id("s1")
        .firstName("John")
        .lastName("Doe")
        .email("john@hei.school")
        .passwordHash("hash")
        .role(Role.STUDENT)
        .createdAt(Instant.now())
        .build();
  }

  @Test
  void generate_with_year_produces_pdf_file() {
    var file = subject.generate(aStudent(), 2025);

    assertTrue(file.exists());
    assertTrue(file.getName().startsWith("transcript-s1"));
    assertTrue(file.getName().endsWith(".pdf"));
    assertTrue(file.length() > 0);
  }

  @Test
  void generate_without_year_produces_pdf_file() {
    var file = subject.generate(aStudent(), null);

    assertTrue(file.exists());
    assertTrue(file.length() > 0);
  }
}
