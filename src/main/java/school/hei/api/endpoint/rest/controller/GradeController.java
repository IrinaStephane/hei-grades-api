package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.model.GradeUpdateRequest;
import school.hei.api.endpoint.rest.security.model.Principal;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;
import school.hei.api.service.GradeService;

@RestController
@AllArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping("/api/grades")
  public List<Grade> getGrades(
      @AuthenticationPrincipal Principal principal,
      @RequestParam(required = false) String studentId,
      @RequestParam(required = false) String examId) {
    return gradeService.get(
        studentId, examId, principal.getUserId(), Role.valueOf(principal.getRole()));
  }

  @GetMapping("/api/grades/{id}")
  public Grade getGradeById(@AuthenticationPrincipal Principal principal, @PathVariable String id) {
    return gradeService.getById(id, principal.getUserId(), Role.valueOf(principal.getRole()));
  }

  @PutMapping("/api/grades/{id}")
  public Grade updateGrade(
      @AuthenticationPrincipal Principal principal,
      @PathVariable String id,
      @Valid @RequestBody GradeUpdateRequest body) {
    return gradeService.update(
        id,
        body.score(),
        body.comment(),
        body.isFinal(),
        principal.getUserId(),
        Role.valueOf(principal.getRole()));
  }

  @GetMapping("/api/grades/{id}/history")
  public List<GradeHistory> getGradeHistory(
      @AuthenticationPrincipal Principal principal, @PathVariable String id) {
    return gradeService.getHistory(id, principal.getUserId(), Role.valueOf(principal.getRole()));
  }
}
