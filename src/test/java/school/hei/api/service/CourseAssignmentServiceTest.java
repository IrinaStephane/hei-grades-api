package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.User;
import school.hei.api.model.dto.CourseAssignmentCreation;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.UserRepository;

class CourseAssignmentServiceTest {

  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final CourseRepository courseRepository = mock(CourseRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final GroupRepository groupRepository = mock(GroupRepository.class);
  private final CourseAssignmentService subject =
      new CourseAssignmentService(
          courseAssignmentRepository, courseRepository, userRepository, groupRepository);

  private static CourseAssignment anAssignment() {
    return CourseAssignment.builder()
        .id("a1")
        .course(Course.builder().id("c1").build())
        .teacher(User.builder().id("t1").build())
        .group(Group.builder().id("g1").build())
        .year(2025)
        .semester(1)
        .build();
  }

  @Test
  void get_all_with_no_filter_returns_everything() {
    var assignments = List.of(anAssignment());
    when(courseAssignmentRepository.findAll()).thenReturn(assignments);

    assertEquals(assignments, subject.getAll(null, null, null));
  }

  @Test
  void get_all_filters_by_teacher_then_group_then_course() {
    var assignments = List.of(anAssignment());
    when(courseAssignmentRepository.findByTeacherId("t1")).thenReturn(assignments);
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(assignments);
    when(courseAssignmentRepository.findByCourseId("c1")).thenReturn(assignments);

    assertEquals(assignments, subject.getAll("t1", null, null));
    assertEquals(assignments, subject.getAll(null, "g1", null));
    assertEquals(assignments, subject.getAll(null, null, "c1"));
  }

  @Test
  void get_by_id_ok() {
    when(courseAssignmentRepository.findById("a1")).thenReturn(Optional.of(anAssignment()));

    assertEquals("a1", subject.getById("a1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(courseAssignmentRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_assignment_ok() {
    var teacher = User.builder().id("t1").role(Role.TEACHER).build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(null).build();
    var course = Course.builder().id("c1").build();
    when(courseRepository.findById("c1")).thenReturn(Optional.of(course));
    when(userRepository.findById("t1")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
    when(courseAssignmentRepository
            .existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester("c1", "t1", "g1", 2025, 1))
        .thenReturn(false);
    when(courseAssignmentRepository.save(any(CourseAssignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create(aCreation("c1", "t1", "g1", 2025, 1));

    assertEquals("c1", created.getCourse().getId());
    assertEquals("t1", created.getTeacher().getId());
    assertEquals("g1", created.getGroup().getId());
  }

  @Test
  void create_assignment_with_unknown_course_not_found() {
    when(courseRepository.findById("c1")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.create(aCreation("c1", "t1", "g1", 2025, 1)));
  }

  @Test
  void create_assignment_with_unknown_teacher_not_found() {
    when(courseRepository.findById("c1")).thenReturn(Optional.of(Course.builder().id("c1").build()));
    when(userRepository.findById("t1")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.create(aCreation("c1", "t1", "g1", 2025, 1)));
  }

  @Test
  void create_assignment_with_non_teacher_user_bad_request() {
    var student = User.builder().id("t1").role(Role.STUDENT).build();
    when(courseRepository.findById("c1")).thenReturn(Optional.of(Course.builder().id("c1").build()));
    when(userRepository.findById("t1")).thenReturn(Optional.of(student));

    assertThrows(BadRequestException.class, () -> subject.create(aCreation("c1", "t1", "g1", 2025, 1)));
  }

  @Test
  void create_duplicate_assignment_conflicts() {
    var teacher = User.builder().id("t1").role(Role.TEACHER).build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(null).build();
    var course = Course.builder().id("c1").build();
    when(courseRepository.findById("c1")).thenReturn(Optional.of(course));
    when(userRepository.findById("t1")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
    when(courseAssignmentRepository
            .existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester("c1", "t1", "g1", 2025, 1))
        .thenReturn(true);

    assertThrows(ConflictException.class, () -> subject.create(aCreation("c1", "t1", "g1", 2025, 1)));
    verify(courseAssignmentRepository, never()).save(any());
  }

  @Test
  void update_assignment_changing_period_checks_duplicates() {
    var assignment = anAssignment();
    var teacher = User.builder().id("t1").role(Role.TEACHER).build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(null).build();
    var course = Course.builder().id("c1").build();
    when(courseAssignmentRepository.findById("a1")).thenReturn(Optional.of(assignment));
    when(courseRepository.findById("c1")).thenReturn(Optional.of(course));
    when(userRepository.findById("t1")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
    when(courseAssignmentRepository
            .existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester("c1", "t1", "g1", 2026, 1))
        .thenReturn(false);
    when(courseAssignmentRepository.save(any(CourseAssignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("a1", aCreation("c1", "t1", "g1", 2026, 1));

    assertEquals(2026, updated.getYear());
  }

  @Test
  void update_assignment_unchanged_skips_duplicate_check() {
    var assignment = anAssignment();
    var teacher = User.builder().id("t1").role(Role.TEACHER).build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(null).build();
    var course = Course.builder().id("c1").build();
    when(courseAssignmentRepository.findById("a1")).thenReturn(Optional.of(assignment));
    when(courseRepository.findById("c1")).thenReturn(Optional.of(course));
    when(userRepository.findById("t1")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
    when(courseAssignmentRepository.save(any(CourseAssignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    subject.update("a1", aCreation("c1", "t1", "g1", 2025, 1));

    verify(courseAssignmentRepository, never())
        .existsByCourseIdAndTeacherIdAndGroupIdAndYearAndSemester(any(), any(), any(), any(), any());
  }

  @Test
  void delete_assignment_ok() {
    when(courseAssignmentRepository.existsById("a1")).thenReturn(true);

    subject.delete("a1");

    verify(courseAssignmentRepository).deleteById("a1");
  }

  @Test
  void delete_unknown_assignment_not_found() {
    when(courseAssignmentRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
    assertTrue(true);
  }

  private static CourseAssignmentCreation aCreation(
      String courseId, String teacherId, String groupId, int year, int semester) {
    return CourseAssignmentCreation.builder()
        .courseId(courseId)
        .teacherId(teacherId)
        .groupId(groupId)
        .year(year)
        .semester(semester)
        .build();
  }
}