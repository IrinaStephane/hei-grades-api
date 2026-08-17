package school.hei.api.file.zip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class FileTyperTest {

  private final FileTyper subject = new FileTyper();

  @Test
  void detects_plain_text() throws Exception {
    var file = File.createTempFile("hello", ".txt");
    Files.writeString(file.toPath(), "Hello, world!");
    file.deleteOnExit();

    assertEquals(MediaType.TEXT_PLAIN, subject.apply(file));
  }

  @Test
  void detects_zip_archive() throws Exception {
    var file = File.createTempFile("archive", ".zip");
    Files.write(file.toPath(), new byte[] {'P', 'K', 3, 4, 0, 0});
    file.deleteOnExit();

    assertEquals("application/zip", subject.apply(file).toString());
  }
}