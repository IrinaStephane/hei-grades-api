package school.hei.api.export;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import school.hei.api.service.GraduateService.Graduate;

@Component
public class XlsxGraduatesExporter {

  public byte[] export(List<Graduate> graduates) {
    try (var workbook = new XSSFWorkbook()) {
      XSSFSheet sheet = workbook.createSheet("Diplômés");
      Row header = sheet.createRow(0);
      String[] columns = {"Rang", "STD", "Nom", "Prénom", "Moyenne générale"};
      for (int i = 0; i < columns.length; i++) {
        header.createCell(i).setCellValue(columns[i]);
      }

      for (int i = 0; i < graduates.size(); i++) {
        var g = graduates.get(i);
        Row row = sheet.createRow(i + 1);
        row.createCell(0).setCellValue(i + 1);
        row.createCell(1).setCellValue(g.getStudentId());
        row.createCell(2).setCellValue(g.getLastName());
        row.createCell(3).setCellValue(g.getFirstName());
        row.createCell(4).setCellValue(Math.round(g.getGeneralAverage() * 100.0) / 100.0);
      }

      var out = new ByteArrayOutputStream();
      workbook.write(out);
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Failed to export graduates to XLSX", e);
    }
  }
}
