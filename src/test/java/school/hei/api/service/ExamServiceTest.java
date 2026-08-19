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
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.model.Exam;

class ExamServiceTest {

  private static final String ADMIN_ID = "admin-1";
  private static final String TEACHER_ID = "teacher-1";

  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final ExamService subject = new ExamService(examRepository, courseAssignmentRepository);

  private static Exam anExam() {
    return Exam.builder()
        .id("exam-1")
        .courseAssignmentId("ca-1")
        .title("Midterm")
        .examinationDate(Instant.parse("2025-06-15T10:00:00Z"))
        .coefficient(0.3)
        .build();
  }

  private static CourseAssignment anAssignment(String id, String teacherId) {
    return CourseAssignment.builder().id(id).teacher(User.builder().id(teacherId).build()).build();
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
  void admin_create_exam_ok() {
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var created =
        subject.create(
            "ca-1", "Final", Instant.parse("2025-07-01T10:00:00Z"), 0.5, ADMIN_ID, Role.ADMIN);

    assertNotNull(created.getId());
    assertEquals("ca-1", created.getCourseAssignmentId());
    assertEquals("Final", created.getTitle());
    assertEquals(0.5, created.getCoefficient());
  }

  @Test
  void create_exam_unknown_assignment_not_found() {
    when(courseAssignmentRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> subject.create("nope", "Final", Instant.now(), 0.5, ADMIN_ID, Role.ADMIN));
    verify(examRepository, never()).save(any());
  }

  @Test
  void teacher_creates_exam_on_own_course_ok() {
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var created =
        subject.create(
            "ca-1", "Final", Instant.parse("2025-07-01T10:00:00Z"), 0.5, TEACHER_ID, Role.TEACHER);

    assertNotNull(created.getId());
  }

  @Test
  void teacher_cannot_create_exam_on_another_course() {
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", "teacher-other")));

    assertThrows(
        ForbiddenException.class,
        () ->
            subject.create(
                "ca-1",
                "Final",
                Instant.parse("2025-07-01T10:00:00Z"),
                0.5,
                TEACHER_ID,
                Role.TEACHER));
    verify(examRepository, never()).save(any());
  }

  @Test
  void admin_update_exam_ok() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated =
        subject.update(
            "exam-1", "Updated", Instant.parse("2025-08-01T10:00:00Z"), 0.7, ADMIN_ID, Role.ADMIN);

    assertEquals("Updated", updated.getTitle());
    assertEquals(0.7, updated.getCoefficient());
  }

  @Test
  void admin_update_unknown_exam_not_found() {
    when(examRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> subject.update("nope", "t", Instant.now(), 0.5, ADMIN_ID, Role.ADMIN));
  }

  @Test
  void teacher_updates_exam_of_own_course_ok() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated =
        subject.update(
            "exam-1",
            "Updated",
            Instant.parse("2025-08-01T10:00:00Z"),
            0.7,
            TEACHER_ID,
            Role.TEACHER);

    assertEquals("Updated", updated.getTitle());
  }

  @Test
  void teacher_cannot_update_exam_of_another_course() {
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam()));
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", "teacher-other")));

    assertThrows(
        ForbiddenException.class,
        () ->
            subject.update(
                "exam-1",
                "Updated",
                Instant.parse("2025-08-01T10:00:00Z"),
                0.7,
                TEACHER_ID,
                Role.TEACHER));
    verify(examRepository, never()).save(any());
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
