package school.hei.api.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.export.XlsxGraduatesExporter;
import school.hei.api.service.GraduateService;
import school.hei.api.service.GraduateService.Graduate;

@RestController
@AllArgsConstructor
public class PromotionGraduatesController {

  private final GraduateService graduateService;
  private final XlsxGraduatesExporter xlsxExporter;

  @GetMapping("/api/promotions/{id}/graduates")
  public List<Graduate> getPromotionGraduates(
      @PathVariable("id") String promotionId, @RequestParam(required = false) String path) {
    return graduateService.getGraduates(promotionId, path);
  }

  @GetMapping("/api/promotions/{id}/graduates/export")
  public ResponseEntity<byte[]> exportPromotionGraduates(
      @PathVariable("id") String promotionId, @RequestParam(required = false) String path) {
    var graduates = graduateService.getGraduates(promotionId, path);
    byte[] xlsx = xlsxExporter.export(graduates);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_TYPE,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=diplomes.xlsx")
        .body(xlsx);
  }
}
