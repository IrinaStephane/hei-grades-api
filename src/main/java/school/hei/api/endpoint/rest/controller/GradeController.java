package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.model.GradeUpdateRequest;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;
import school.hei.api.service.GradeService;

@RestController
@AllArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping("/api/grades")
  public List<Grade> getGrades(
      @RequestParam(required = false) String studentId,
      @RequestParam(required = false) String examId) {
    return gradeService.get(studentId, examId);
  }

  @GetMapping("/api/grades/{id}")
  public Grade getGradeById(@PathVariable String id) {
    return gradeService.getById(id);
  }

  @PutMapping("/api/grades/{id}")
  public Grade updateGrade(@PathVariable String id, @Valid @RequestBody GradeUpdateRequest body) {
    return gradeService.update(id, body.score(), body.comment(), body.isFinal());
  }

  @GetMapping("/api/grades/{id}/history")
  public List<GradeHistory> getGradeHistory(@PathVariable String id) {
    return gradeService.getHistory(id);
  }
}
