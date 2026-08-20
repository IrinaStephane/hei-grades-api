package school.hei.api.endpoint.rest.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.export.XlsxGraduatesExporter;
import school.hei.api.file.bucket.BucketComponent;
import school.hei.api.service.GraduateService;
import school.hei.api.service.GraduateService.Graduate;

@RestController
@AllArgsConstructor
public class PromotionGraduatesController {

  private final GraduateService graduateService;
  private final XlsxGraduatesExporter xlsxExporter;
  private final BucketComponent bucketComponent;

  @GetMapping("/promotions/{id}/graduates")
  public List<Graduate> getPromotionGraduates(
      @PathVariable("id") String promotionId, @RequestParam(required = false) String path) {
    return graduateService.getGraduates(promotionId, path);
  }

  @GetMapping("/promotions/{id}/graduates/export")
  public ResponseEntity<Void> exportPromotionGraduates(
      @PathVariable("id") String promotionId, @RequestParam(required = false) String path) {
    var graduates = graduateService.getGraduates(promotionId, path);
    byte[] xlsx = xlsxExporter.export(graduates);

    File tempFile = null;
    try {
      tempFile = File.createTempFile("graduates-" + promotionId, ".xlsx");
      try (var os = new FileOutputStream(tempFile)) {
        os.write(xlsx);
      }

      String bucketKey = "graduates/" + promotionId + "/diplomes.xlsx";
      bucketComponent.upload(tempFile, bucketKey);
      var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofHours(1));

      return ResponseEntity.status(302).location(URI.create(presignedUrl.toString())).build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to export graduates to XLSX via S3", e);
    } finally {
      if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
        tempFile.deleteOnExit();
      }
    }
  }
}
