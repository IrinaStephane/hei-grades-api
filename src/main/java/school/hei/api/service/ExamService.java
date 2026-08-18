package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.model.Exam;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;

  public List<Exam> getByCourseAssignment(String courseAssignmentId) {
    return courseAssignmentId == null
        ? examRepository.findAll()
        : examRepository.findByCourseAssignmentId(courseAssignmentId);
  }

  public Exam getById(String id) {
    return examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam", id));
  }

  public Exam create(
      String courseAssignmentId, String title, Instant examinationDate, Double coefficient) {
    var exam =
        Exam.builder()
            .id(UUID.randomUUID().toString())
            .courseAssignmentId(courseAssignmentId)
            .title(title)
            .examinationDate(examinationDate)
            .coefficient(coefficient)
            .build();
    return examRepository.save(exam);
  }

  public Exam update(String id, String title, Instant examinationDate, Double coefficient) {
    var exam = getById(id);
    exam.setTitle(title);
    exam.setExaminationDate(examinationDate);
    exam.setCoefficient(coefficient);
    return examRepository.save(exam);
  }

  public void delete(String id) {
    examRepository.delete(getById(id));
  }
}
