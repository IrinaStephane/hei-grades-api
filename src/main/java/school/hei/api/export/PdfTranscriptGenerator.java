package school.hei.api.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import org.springframework.stereotype.Component;
import school.hei.api.model.User;

@Component
public class PdfTranscriptGenerator {

  public File generate(User student, Integer year) {
    try {
      String html = buildHtml(student, year);
      File file = File.createTempFile("transcript-" + student.getId(), ".pdf");
      try (var os = new FileOutputStream(file)) {
        var builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();
      }
      return file;
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate transcript PDF", e);
    }
  }

  private String buildHtml(User student, Integer year) {
    String scope = year == null ? "cumulé (3 ans)" : "année " + year;
    return "<html><body>"
        + "<h1>Relevé de notes — "
        + student.getFirstName()
        + " "
        + student.getLastName()
        + "</h1>"
        + "<p>Période : "
        + scope
        + "</p>"
        + "</body></html>";
  }
}
