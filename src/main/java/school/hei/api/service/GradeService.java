package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;

  public List<Grade> get(String studentId, String examId) {
    if (studentId != null) return gradeRepository.findByStudentId(studentId);
    if (examId != null) return gradeRepository.findByExamId(examId);
    return gradeRepository.findAll();
  }

  public Grade getById(String id) {
    return gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade", id));
  }

  @Transactional
  public Grade update(String id, Double newScore, String comment, Boolean isFinal) {
    var grade = getById(id);
    var oldScore = grade.getScore();

    grade.setScore(newScore);
    if (isFinal != null) {
      grade.setFinal(isFinal);
    }
    var saved = gradeRepository.save(grade);

    gradeHistoryRepository.save(
        GradeHistory.builder()
            .id(UUID.randomUUID().toString())
            .gradeId(saved.getId())
            .oldScore(oldScore)
            .newScore(newScore)
            .changedAt(Instant.now())
            .comment(comment)
            .build());

    return saved;
  }

  public List<GradeHistory> getHistory(String gradeId) {
    getById(gradeId);
    return gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc(gradeId);
  }
}
