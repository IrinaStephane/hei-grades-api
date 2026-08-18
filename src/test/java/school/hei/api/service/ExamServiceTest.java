package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.endpoint.rest.model.ApiExceptions.NotFoundException;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.model.Exam;

class ExamServiceTest {

  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final ExamService subject = new ExamService(examRepository);

  private static Exam anExam() {
    return Exam.builder()
        .id("exam-1")
        .courseAssignmentId("ca-1")
        .title("Midterm")
        .examinationDate(Instant.parse("2025-06-15T10:00:00Z"))
        .coefficient(0.3)
        .build();
  }

  @Test
  void get_by_course_assignment_filters() {
    var exams = List.of(anExam());
    when(examRepository.findByCourseAssignmentId("ca-1")).thenReturn(exams);

    assertEquals(exams, subject.getByCourseAssignment("ca-1"));
  }

  @Test
  void get_all_when_no_filter() {
    var exams = List.of(anExam());
    when(examRepository.findAll()).thenReturn(exams);

    assertEquals(exams, subject.getByCourseAssignment(null));
  }

  @Test
  void get_by_id_ok() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));

    assertEquals("exam-1", subject.getById("exam-1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(examRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_exam_ok() {
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create("ca-1", "Final", Instant.parse("2025-07-01T10:00:00Z"), 0.5);

    assertNotNull(created.getId());
    assertEquals("ca-1", created.getCourseAssignmentId());
    assertEquals("Final", created.getTitle());
    assertEquals(0.5, created.getCoefficient());
  }

  @Test
  void update_exam_ok() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("exam-1", "Updated", Instant.parse("2025-08-01T10:00:00Z"), 0.7);

    assertEquals("Updated", updated.getTitle());
    assertEquals(0.7, updated.getCoefficient());
  }

  @Test
  void update_unknown_exam_not_found() {
    when(examRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.update("nope", "t", Instant.now(), 0.5));
  }

  @Test
  void delete_exam_ok() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));

    subject.delete("exam-1");

    verify(examRepository).delete(any(Exam.class));
  }

  @Test
  void delete_unknown_exam_not_found() {
    when(examRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
    verify(examRepository, never()).delete(any());
  }
}
