package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.model.ExamCreationRequest;
import school.hei.api.repository.model.Exam;
import school.hei.api.service.ExamService;

@RestController
@AllArgsConstructor
public class ExamController {

  private final ExamService examService;

  @GetMapping("/api/exams")
  public List<Exam> getExams(@RequestParam(required = false) String courseAssignmentId) {
    return examService.getByCourseAssignment(courseAssignmentId);
  }

  @GetMapping("/api/exams/{id}")
  public Exam getExamById(@PathVariable String id) {
    return examService.getById(id);
  }

  @PostMapping("/api/exams")
  @ResponseStatus(HttpStatus.CREATED)
  public Exam createExam(@Valid @RequestBody ExamCreationRequest body) {
    return examService.create(
        body.courseAssignmentId(), body.title(), body.examinationDate(), body.coefficient());
  }

  @PutMapping("/api/exams/{id}")
  public Exam updateExam(@PathVariable String id, @Valid @RequestBody ExamCreationRequest body) {
    return examService.update(id, body.title(), body.examinationDate(), body.coefficient());
  }

  @DeleteMapping("/api/exams/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExam(@PathVariable String id) {
    examService.delete(id);
  }
}
