package school.hei.api.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.User;
import school.hei.api.model.dto.CourseAssignmentCreation;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {

  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public List<CourseAssignment> getAll(String teacherId, String groupId, String courseId) {
    List<CourseAssignment> assignments;
    if (teacherId != null) {
      assignments = courseAssignmentRepository.findByTeacherId(teacherId);
    } else if (groupId != null) {
      assignments = courseAssignmentRepository.findByGroupId(groupId);
    } else if (courseId != null) {
      assignments = courseAssignmentRepository.findByCourseId(courseId);
    } else {
      assignments = courseAssignmentRepository.findAll();
    }
    return assignments;
  }

  public CourseAssignment getById(String id) {
    return getEntityById(id);
  }

  @Transactional
  public CourseAssignment create(CourseAssignmentCreation creation) {
    Course course = getCourseOrThrow(creation.getCourseId());
    User teacher = getTeacherOrThrow(creation.getTeacherId());
    Group group = getGroupOrThrow(creation.getGroupId());
    assertNoDuplicate(
        course.getId(), teacher.getId(), group.getId(), creation.getYear(), creation.getSemester());

    CourseAssignment assignment =
        CourseAssignment.builder()
            .course(course)
            .teacher(teacher)
            .group(group)
            .year(creation.getYear())
            .semester(creation.getSemester())
            .build();
    return courseAssignmentRepository.save(assignment);
  }

  @Transactional
  public CourseAssignment update(String id, CourseAssignmentCreation creation) {
    CourseAssignment assignment = getEntityById(id);
    Course course = getCourseOrThrow(creation.getCourseId());
    User teacher = getTeacherOrThrow(creation.getTeacherId());
    Group group = getGroupOrThrow(creation.getGroupId());

    boolean unchanged =
        assignment.getCourse().getId().equals(course.getId())
            && assignment.getTeacher().getId().equals(teacher.getId())
            && assignment.getGroup().getId().equals(group.getId())
            && assignment.getYear().equals(creation.getYear())
            && assignment.getSemester().equals(creation.getSemester());
    if (!unchanged) {
      assertNoDuplicate(
          course.getId(),
          teacher.getId(),
          group.getId(),
          creation.getYear(),
          creation.getSemester());
    }

    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setGroup(group);
    assignment.setYear(creation.getYear());
    assignment.setSemester(creation.getSemester());
    return courseAssignmentRepository.save(assignment);
  }

  @Transactional
  public void delete(String id) {
    if (!courseAssignmentRepository.existsById(id)) {
      throw new NotFoundException("CourseAssignment " + id + " not found");
    }
    courseAssignmentRepository.deleteById(id);
  }

  private void assertNoDuplicate(
      String courseId, String teacherId, String groupId, Integer year, Integer semester) {
    boolean exists =
        courseAssignmentRepository.existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester(
            courseId, teacherId, groupId, year, semester);
    if (exists) {
      throw new ConflictException(
          "This course/teacher/group is already assigned for year "
              + year
              + " semester "
              + semester);
    }
  }

  private CourseAssignment getEntityById(String id) {
    return courseAssignmentRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("CourseAssignment " + id + " not found"));
  }

  private Course getCourseOrThrow(String courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));
  }

  private User getTeacherOrThrow(String teacherId) {
    User teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new NotFoundException("User " + teacherId + " not found"));
    if (teacher.getRole() != Role.TEACHER) {
      throw new BadRequestException("User " + teacherId + " is not a teacher");
    }
    return teacher;
  }

  private Group getGroupOrThrow(String groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group " + groupId + " not found"));
  }
}