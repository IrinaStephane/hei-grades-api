package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.UserRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;
import school.hei.api.repository.model.GradeHistory;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final UserRepository userRepository;

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

  public Grade create(
      String examId,
      String studentId,
      Double score,
      Boolean isFinal,
      String authenticatedUserId,
      Role authenticatedRole) {
    if (authenticatedRole == Role.STUDENT) {
      throw new ForbiddenException("A student cannot create grades");
    }
    if (authenticatedRole == Role.TEACHER) {
      var exam =
          examRepository
              .findById(examId)
              .orElseThrow(() -> new NotFoundException("Exam with id " + examId + " not found"));
      if (!isOwnedByTeacher(exam, authenticatedUserId)) {
        throw new ForbiddenException("A teacher can only create grades for their own courses");
      }
    }
    examRepository
        .findById(examId)
        .orElseThrow(() -> new NotFoundException("Exam with id " + examId + " not found"));
    if (!userRepository.existsById(studentId)) {
      throw new BadRequestException("Student with id " + studentId + " not found");
    }
    if (gradeRepository.existsByExamIdAndStudentId(examId, studentId)) {
      throw new ConflictException(
          "A grade already exists for exam " + examId + " and student " + studentId);
    }

    var grade =
        Grade.builder()
            .id(UUID.randomUUID().toString())
            .examId(examId)
            .studentId(studentId)
            .score(score)
            .isFinal(isFinal != null && isFinal)
            .build();
    return gradeRepository.save(grade);
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
    boolean scoreChanged = !java.util.Objects.equals(oldScore, newScore);
    boolean isFinalChanged = isFinal != null && grade.isFinal() != isFinal;

    grade.setScore(newScore);
    if (isFinal != null) {
      grade.setFinal(isFinal);
    }
    var saved = gradeRepository.save(grade);

    if (scoreChanged || isFinalChanged) {
      gradeHistoryRepository.save(
          GradeHistory.builder()
              .id(UUID.randomUUID().toString())
              .gradeId(saved.getId())
              .oldScore(oldScore)
              .newScore(newScore)
              .changedAt(Instant.now())
              .comment(comment)
              .build());
    }

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
        .map(exam -> isOwnedByTeacher(exam, teacherId))
        .orElse(false);
  }

  private boolean isOwnedByTeacher(Exam exam, String teacherId) {
    return courseAssignmentRepository
        .findById(exam.getCourseAssignmentId())
        .map(assignment -> assignment.getTeacher().getId().equals(teacherId))
        .orElse(false);
  }

  private Grade getEntityById(String id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Grade with id " + id + " not found"));
  }
}
