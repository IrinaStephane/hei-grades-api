package school.hei.api.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.service.GraduateService.CourseResult;
import school.hei.api.service.GraduateService.Graduate;

class XlsxGraduatesExporterTest {

  private final XlsxGraduatesExporter subject = new XlsxGraduatesExporter();

  private static Graduate aGraduate() {
    return Graduate.builder()
        .studentId("s1")
        .firstName("John")
        .lastName("Doe")
        .path("EL")
        .generalAverage(15.5)
        .results(List.of(CourseResult.builder().courseId("c1").credits(3).finalGrade(15.0).build()))
        .build();
  }

  @Test
  void export_empty_list_produces_valid_xlsx() {
    byte[] result = subject.export(List.of());

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  void export_with_graduates_produces_valid_xlsx() {
    byte[] result = subject.export(List.of(aGraduate(), aGraduate()));

    assertNotNull(result);
    assertTrue(result.length > 0);
  }
}
