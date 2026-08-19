package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

  public List<Grade> get(
      String studentId, String examId, String authenticatedUserId, Role authenticatedRole) {
    if (authenticatedRole == Role.ADMIN) {
      return getAsAdmin(studentId, examId);
    }
    if (authenticatedRole == Role.STUDENT) {
      return getAsStudent(authenticatedUserId, examId);
    }
    return getAsTeacher(authenticatedUserId, studentId, examId);
  }

  public Grade getById(String id, String authenticatedUserId, Role authenticatedRole) {
    Grade grade = getEntityById(id);
    assertCanRead(grade, authenticatedUserId, authenticatedRole);
    return grade;
  }

  @Transactional
  public Grade update(
      String id,
      Double newScore,
      String comment,
      Boolean isFinal,
      String authenticatedUserId,
      Role authenticatedRole) {
    var grade = getEntityById(id);
    assertCanWrite(grade, authenticatedUserId, authenticatedRole);
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

  public List<GradeHistory> getHistory(
      String gradeId, String authenticatedUserId, Role authenticatedRole) {
    Grade grade = getEntityById(gradeId);
    assertCanRead(grade, authenticatedUserId, authenticatedRole);
    return gradeHistoryRepository.findByGradeIdOrderByChangedAtDesc(gradeId);
  }

  private List<Grade> getAsAdmin(String studentId, String examId) {
    if (studentId != null && examId != null) {
      return gradeRepository.findByStudentIdAndExamId(studentId, examId);
    }
    if (studentId != null) {
      return gradeRepository.findByStudentId(studentId);
    }
    if (examId != null) {
      return gradeRepository.findByExamId(examId);
    }
    return gradeRepository.findAll();
  }

  private List<Grade> getAsStudent(String studentId, String examId) {
    return examId == null
        ? gradeRepository.findByStudentId(studentId)
        : gradeRepository.findByStudentIdAndExamId(studentId, examId);
  }

  private List<Grade> getAsTeacher(String teacherId, String studentId, String examId) {
    List<String> assignmentIds =
        courseAssignmentRepository.findByTeacherId(teacherId).stream()
            .map(CourseAssignment::getId)
            .toList();
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    return gradeRepository.findByExamCourseAssignmentIds(assignmentIds).stream()
        .filter(grade -> studentId == null || grade.getStudentId().equals(studentId))
        .filter(grade -> examId == null || grade.getExamId().equals(examId))
        .toList();
  }

  private void assertCanRead(Grade grade, String userId, Role role) {
    if (role == Role.ADMIN) {
      return;
    }
    if (role == Role.STUDENT) {
      if (!grade.getStudentId().equals(userId)) {
        throw new ForbiddenException("A student can only view their own grades");
      }
      return;
    }
    assertTeacherOwns(grade, userId);
  }

  private void assertCanWrite(Grade grade, String userId, Role role) {
    if (role == Role.ADMIN) {
      return;
    }
    if (role == Role.STUDENT) {
      throw new ForbiddenException("A student cannot update grades");
    }
    assertTeacherOwns(grade, userId);
  }

  private void assertTeacherOwns(Grade grade, String teacherId) {
    if (!isOwnedByTeacher(grade, teacherId)) {
      throw new ForbiddenException("A teacher can only access grades of their own courses");
    }
  }

  private boolean isOwnedByTeacher(Grade grade, String teacherId) {
    return examRepository
        .findById(grade.getExamId())
        .flatMap(exam -> courseAssignmentRepository.findById(exam.getCourseAssignmentId()))
        .map(assignment -> assignment.getTeacher().getId().equals(teacherId))
        .orElse(false);
  }

  private Grade getEntityById(String id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Grade with id " + id + " not found"));
  }
}
