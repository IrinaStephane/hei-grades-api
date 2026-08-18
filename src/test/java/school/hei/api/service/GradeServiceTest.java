package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;

class GradeServiceTest {

  private final GradeRepository gradeRepository = mock(GradeRepository.class);
  private final GradeHistoryRepository gradeHistoryRepository = mock(GradeHistoryRepository.class);
  private final GradeService subject = new GradeService(gradeRepository, gradeHistoryRepository);

  private static Grade aGrade() {
    return Grade.builder()
        .id("g1")
        .examId("exam-1")
        .studentId("s1")
        .score(14.0)
        .isFinal(false)
        .build();
  }

  @Test
  void get_with_student_id() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByStudentId("s1")).thenReturn(grades);

    assertEquals(grades, subject.get("s1", null));
  }

  @Test
  void get_with_exam_id() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByExamId("exam-1")).thenReturn(grades);

    assertEquals(grades, subject.get(null, "exam-1"));
  }

  @Test
  void get_all_when_no_filter() {
    var grades = List.of(aGrade());
    when(gradeRepository.findAll()).thenReturn(grades);

    assertEquals(grades, subject.get(null, null));
  }

  @Test
  void get_by_id_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertEquals("g1", subject.getById("g1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void update_score_creates_history() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("g1", 16.0, "Good work", true);

    assertEquals(16.0, updated.getScore());
    assertTrue(updated.isFinal());
    verify(gradeHistoryRepository).save(any(GradeHistory.class));
  }

  @Test
  void update_score_with_null_is_final_keeps_previous() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("g1", 12.0, "Changed", null);

    assertEquals(12.0, updated.getScore());
    assertFalse(updated.isFinal());
  }

  @Test
  void update_unknown_grade_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.update("nope", 10.0, "x", false));
    verify(gradeHistoryRepository, never()).save(any());
  }

  @Test
  void get_history_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    var history = List.of(GradeHistory.builder().id("h1").gradeId("g1").build());
    when(gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc("g1")).thenReturn(history);

    var result = subject.getHistory("g1");

    assertEquals(1, result.size());
    assertEquals("h1", result.get(0).getId());
  }

  @Test
  void get_history_unknown_grade_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getHistory("nope"));
  }
}
