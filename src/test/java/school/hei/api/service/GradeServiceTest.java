package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;

class GradeServiceTest {

  private static final String ADMIN_ID = "admin-1";
  private static final String TEACHER_ID = "teacher-1";
  private static final String STUDENT_ID = "student-1";

  private final GradeRepository gradeRepository = mock(GradeRepository.class);
  private final GradeHistoryRepository gradeHistoryRepository = mock(GradeHistoryRepository.class);
  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final GradeService subject =
      new GradeService(
          gradeRepository, gradeHistoryRepository, examRepository, courseAssignmentRepository);

  private static Grade aGrade() {
    return Grade.builder()
        .id("g1")
        .examId("exam-1")
        .studentId(STUDENT_ID)
        .score(14.0)
        .isFinal(false)
        .build();
  }

  private static Exam anExam(String id, String courseAssignmentId) {
    return Exam.builder().id(id).courseAssignmentId(courseAssignmentId).build();
  }

  private static CourseAssignment anAssignment(String id, String teacherId) {
    return CourseAssignment.builder().id(id).teacher(User.builder().id(teacherId).build()).build();
  }

  @Test
  void admin_get_with_student_id() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByStudentId(STUDENT_ID)).thenReturn(grades);

    assertEquals(grades, subject.get(STUDENT_ID, null, ADMIN_ID, Role.ADMIN));
  }

  @Test
  void admin_get_with_exam_id() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByExamId("exam-1")).thenReturn(grades);

    assertEquals(grades, subject.get(null, "exam-1", ADMIN_ID, Role.ADMIN));
  }

  @Test
  void admin_get_all_when_no_filter() {
    var grades = List.of(aGrade());
    when(gradeRepository.findAll()).thenReturn(grades);

    assertEquals(grades, subject.get(null, null, ADMIN_ID, Role.ADMIN));
  }

  @Test
  void admin_get_with_student_and_exam() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByStudentIdAndExamId(STUDENT_ID, "exam-1")).thenReturn(grades);

    assertEquals(grades, subject.get(STUDENT_ID, "exam-1", ADMIN_ID, Role.ADMIN));
  }

  @Test
  void student_get_returns_only_own_grades() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByStudentId(STUDENT_ID)).thenReturn(grades);

    var result = subject.get("someone-else", null, STUDENT_ID, Role.STUDENT);

    assertEquals(grades, result);
    verify(gradeRepository).findByStudentId(STUDENT_ID);
    verify(gradeRepository, never()).findAll();
  }

  @Test
  void student_get_with_exam_filters_own_grades() {
    var grades = List.of(aGrade());
    when(gradeRepository.findByStudentIdAndExamId(STUDENT_ID, "exam-1")).thenReturn(grades);

    assertEquals(grades, subject.get(null, "exam-1", STUDENT_ID, Role.STUDENT));
    verify(gradeRepository).findByStudentIdAndExamId(STUDENT_ID, "exam-1");
  }

  @Test
  void teacher_get_returns_only_grades_of_own_courses() {
    var ownGrade = aGrade();
    when(courseAssignmentRepository.findByTeacherId(TEACHER_ID))
        .thenReturn(List.of(anAssignment("ca-1", TEACHER_ID)));
    when(gradeRepository.findByExamCourseAssignmentIds(List.of("ca-1")))
        .thenReturn(List.of(ownGrade));

    var result = subject.get(null, null, TEACHER_ID, Role.TEACHER);

    assertEquals(List.of(ownGrade), result);
    verify(gradeRepository, never()).findAll();
  }

  @Test
  void teacher_get_without_assignments_returns_empty() {
    when(courseAssignmentRepository.findByTeacherId(TEACHER_ID)).thenReturn(List.of());

    var result = subject.get(null, null, TEACHER_ID, Role.TEACHER);

    assertTrue(result.isEmpty());
    verify(gradeRepository, never()).findByExamCourseAssignmentIds(any());
  }

  @Test
  void teacher_get_filters_by_student_and_exam() {
    var gradeOfStudent1 = aGrade();
    var gradeOfStudent2 = Grade.builder().id("g2").examId("exam-1").studentId("student-2").build();
    when(courseAssignmentRepository.findByTeacherId(TEACHER_ID))
        .thenReturn(List.of(anAssignment("ca-1", TEACHER_ID)));
    when(gradeRepository.findByExamCourseAssignmentIds(List.of("ca-1")))
        .thenReturn(List.of(gradeOfStudent1, gradeOfStudent2));

    var result = subject.get(STUDENT_ID, "exam-1", TEACHER_ID, Role.TEACHER);

    assertEquals(List.of(gradeOfStudent1), result);
  }

  @Test
  void admin_get_by_id_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertEquals("g1", subject.getById("g1", ADMIN_ID, Role.ADMIN).getId());
  }

  @Test
  void admin_get_by_unknown_id_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope", ADMIN_ID, Role.ADMIN));
  }

  @Test
  void student_reads_own_grade_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertEquals("g1", subject.getById("g1", STUDENT_ID, Role.STUDENT).getId());
  }

  @Test
  void student_cannot_read_another_student_grade() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertThrows(
        ForbiddenException.class, () -> subject.getById("g1", "student-other", Role.STUDENT));
  }

  @Test
  void teacher_reads_grade_of_own_course_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam("exam-1", "ca-1")));
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));

    assertEquals("g1", subject.getById("g1", TEACHER_ID, Role.TEACHER).getId());
  }

  @Test
  void teacher_cannot_read_grade_of_another_course() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam("exam-1", "ca-2")));
    when(courseAssignmentRepository.findById("ca-2"))
        .thenReturn(Optional.of(anAssignment("ca-2", "teacher-other")));

    assertThrows(ForbiddenException.class, () -> subject.getById("g1", TEACHER_ID, Role.TEACHER));
  }

  @Test
  void admin_update_score_creates_history() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("g1", 16.0, "Good work", true, ADMIN_ID, Role.ADMIN);

    assertEquals(16.0, updated.getScore());
    assertTrue(updated.isFinal());
    verify(gradeHistoryRepository).save(any(GradeHistory.class));
  }

  @Test
  void admin_update_score_with_null_is_final_keeps_previous() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("g1", 12.0, "Changed", null, ADMIN_ID, Role.ADMIN);

    assertEquals(12.0, updated.getScore());
    assertFalse(updated.isFinal());
  }

  @Test
  void admin_update_unknown_grade_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> subject.update("nope", 10.0, "x", false, ADMIN_ID, Role.ADMIN));
    verify(gradeHistoryRepository, never()).save(any());
  }

  @Test
  void student_cannot_update_grade() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertThrows(
        ForbiddenException.class,
        () -> subject.update("g1", 15.0, "x", false, STUDENT_ID, Role.STUDENT));
    verify(gradeRepository, never()).save(any());
  }

  @Test
  void teacher_cannot_update_grade_of_another_course() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam("exam-1", "ca-2")));
    when(courseAssignmentRepository.findById("ca-2"))
        .thenReturn(Optional.of(anAssignment("ca-2", "teacher-other")));

    assertThrows(
        ForbiddenException.class,
        () -> subject.update("g1", 15.0, "x", false, TEACHER_ID, Role.TEACHER));
    verify(gradeRepository, never()).save(any());
  }

  @Test
  void teacher_updates_grade_of_own_course_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    when(examRepository.findById("exam-1")).thenReturn(Optional.of(anExam("exam-1", "ca-1")));
    when(courseAssignmentRepository.findById("ca-1"))
        .thenReturn(Optional.of(anAssignment("ca-1", TEACHER_ID)));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("g1", 17.0, "Reclamation", null, TEACHER_ID, Role.TEACHER);

    assertEquals(17.0, updated.getScore());
    verify(gradeHistoryRepository).save(any(GradeHistory.class));
  }

  @Test
  void admin_get_history_ok() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));
    var history = List.of(GradeHistory.builder().id("h1").gradeId("g1").build());
    when(gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc("g1")).thenReturn(history);

    var result = subject.getHistory("g1", ADMIN_ID, Role.ADMIN);

    assertEquals(1, result.size());
    assertEquals("h1", result.get(0).getId());
  }

  @Test
  void admin_get_history_unknown_grade_not_found() {
    when(gradeRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getHistory("nope", ADMIN_ID, Role.ADMIN));
  }

  @Test
  void student_cannot_read_history_of_another_student() {
    when(gradeRepository.findById("g1")).thenReturn(Optional.of(aGrade()));

    assertThrows(
        ForbiddenException.class, () -> subject.getHistory("g1", "student-other", Role.STUDENT));
    verify(gradeHistoryRepository, never()).findByGradeIdOrderByChangedAtDesc(eq("g1"));
  }
}
