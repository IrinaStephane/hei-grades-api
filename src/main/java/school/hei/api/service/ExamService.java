package school.hei.api.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.model.Exam;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

  public List<Exam> getByCourseAssignment(String courseAssignmentId) {
    return courseAssignmentId == null
        ? examRepository.findAll()
        : examRepository.findByCourseAssignmentId(courseAssignmentId);
  }

  public Exam getById(String id) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Exam with id " + id + " not found"));
  }

  public Exam create(
      String courseAssignmentId,
      String title,
      Instant examinationDate,
      Double coefficient,
      String authenticatedUserId,
      Role authenticatedRole) {
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "CourseAssignment with id " + courseAssignmentId + " not found"));
    assertCanManage(assignment, authenticatedUserId, authenticatedRole);

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

  public Exam update(
      String id,
      String title,
      Instant examinationDate,
      Double coefficient,
      String authenticatedUserId,
      Role authenticatedRole) {
    var exam = getById(id);
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(exam.getCourseAssignmentId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "CourseAssignment with id " + exam.getCourseAssignmentId() + " not found"));
    assertCanManage(assignment, authenticatedUserId, authenticatedRole);

    exam.setTitle(title);
    exam.setExaminationDate(examinationDate);
    exam.setCoefficient(coefficient);
    return examRepository.save(exam);
  }

  public void delete(String id) {
    examRepository.delete(getById(id));
  }

  private void assertCanManage(
      CourseAssignment assignment, String authenticatedUserId, Role authenticatedRole) {
    if (authenticatedRole == Role.ADMIN) {
      return;
    }
    if (authenticatedRole == Role.TEACHER
        && !assignment.getTeacher().getId().equals(authenticatedUserId)) {
      throw new ForbiddenException("A teacher can only manage exams of their own courses");
    }
  }
}
