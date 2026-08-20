package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.UserRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;

class GraduateServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final GroupRepository groupRepository = mock(GroupRepository.class);
  private final GroupFlowRepository groupFlowRepository = mock(GroupFlowRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final CourseRepository courseRepository = mock(CourseRepository.class);
  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final GradeRepository gradeRepository = mock(GradeRepository.class);
  private final GraduateService subject =
      new GraduateService(
          userRepository,
          groupRepository,
          groupFlowRepository,
          courseAssignmentRepository,
          courseRepository,
          examRepository,
          gradeRepository);

  private static User aStudent(String id, String firstName, String lastName) {
    return User.builder().id(id).firstName(firstName).lastName(lastName).build();
  }

  private static Group aGroup(String id, Path path) {
    return Group.builder().id(id).ref("K1").path(path).build();
  }

  private static GroupFlow aJoinFlow(String groupId, User student) {
    return GroupFlow.builder()
        .id("f-" + groupId + "-" + student.getId())
        .group(aGroup(groupId, Path.EL))
        .student(student)
        .flowType(FlowType.JOIN)
        .flowDatetime(java.time.Instant.parse("2024-01-01T00:00:00Z"))
        .build();
  }

  private static Course aCourse(String id, String code, int credits) {
    return Course.builder().id(id).code(code).title("Course " + code).credits(credits).build();
  }

  private static CourseAssignment anAssignment(String id, Course course, Group group) {
    return CourseAssignment.builder()
        .id(id)
        .course(course)
        .teacher(User.builder().id("teacher-1").build())
        .group(group)
        .year(2024)
        .semester(1)
        .build();
  }

  private static Exam anExam(String id, String caId, double coefficient) {
    return Exam.builder()
        .id(id)
        .courseAssignmentId(caId)
        .coefficient(coefficient)
        .title("Exam")
        .build();
  }

  private static Grade aGrade(String id, String examId, String studentId, double score) {
    return Grade.builder()
        .id(id)
        .examId(examId)
        .studentId(studentId)
        .score(score)
        .isFinal(true)
        .build();
  }

  @Test
  void graduates_sorted_by_average_descending() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student1 = aStudent("s1", "Alice", "A");
    var student2 = aStudent("s2", "Bob", "B");
    var course = aCourse("c1", "INF101", 30);
    var assignment = anAssignment("ca1", course, group);
    var exam1 = anExam("e1", "ca1", 1.0);

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(aJoinFlow("g1", student1), aJoinFlow("g1", student2)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(assignment));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam1));
    when(gradeRepository.findByExamId("e1"))
        .thenReturn(List.of(aGrade("g1", "e1", "s1", 18.0), aGrade("g2", "e1", "s2", 12.0)));

    var graduates = subject.getGraduates(promoId, "EL");

    assertEquals(2, graduates.size());
    assertEquals("s1", graduates.get(0).getStudentId());
    assertEquals("s2", graduates.get(1).getStudentId());
    assertTrue(graduates.get(0).getGeneralAverage() > graduates.get(1).getGeneralAverage());
  }

  @Test
  void weighted_average_by_credits() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student = aStudent("s1", "Alice", "A");
    var course1 = aCourse("c1", "INF101", 30);
    var course2 = aCourse("c2", "INF102", 60);
    var ca1 = anAssignment("ca1", course1, group);
    var ca2 = anAssignment("ca2", course2, group);
    var exam1 = anExam("e1", "ca1", 1.0);
    var exam2 = anExam("e2", "ca2", 1.0);

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(aJoinFlow("g1", student)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(ca1, ca2));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam1));
    when(examRepository.findByCourseAssignmentId("ca2")).thenReturn(List.of(exam2));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(aGrade("g1", "e1", "s1", 20.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(aGrade("g2", "e2", "s1", 10.0)));

    var graduates = subject.getGraduates(promoId, "EL");

    assertEquals(1, graduates.size());
    // 20*30 + 10*60 = 600 + 600 = 1200 / 90 = 13.33
    assertEquals(13.33, graduates.get(0).getGeneralAverage(), 0.01);
  }

  @Test
  void excludes_student_with_course_below_10() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student = aStudent("s1", "Alice", "A");
    var course = aCourse("c1", "INF101", 30);
    var assignment = anAssignment("ca1", course, group);
    var exam = anExam("e1", "ca1", 1.0);

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(aJoinFlow("g1", student)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(assignment));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(aGrade("g1", "e1", "s1", 8.0)));

    var graduates = subject.getGraduates(promoId, "EL");

    assertTrue(graduates.isEmpty());
  }

  @Test
  void excludes_student_without_results() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student = aStudent("s1", "Alice", "A");

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(aJoinFlow("g1", student)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());

    var graduates = subject.getGraduates(promoId, "EL");

    assertTrue(graduates.isEmpty());
  }

  @Test
  void filters_by_path_el() {
    var promoId = "promo-1";
    var groupEl = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var groupTn = Group.builder().id("g2").ref("K2").path(Path.TN).build();
    var studentEl = aStudent("s1", "Alice", "A");
    var studentTn = aStudent("s2", "Bob", "B");
    var course = aCourse("c1", "INF101", 30);
    var caEl = anAssignment("ca1", course, groupEl);
    var caTn = anAssignment("ca2", course, groupTn);
    var exam = anExam("e1", "ca1", 1.0);
    var examTn = anExam("e2", "ca2", 1.0);

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(groupEl, groupTn));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(aJoinFlow("g1", studentEl)));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2"))
        .thenReturn(List.of(aJoinFlow("g2", studentTn)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(caEl));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of(caTn));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam));
    when(examRepository.findByCourseAssignmentId("ca2")).thenReturn(List.of(examTn));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(aGrade("g1", "e1", "s1", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(aGrade("g2", "e2", "s2", 15.0)));

    var graduates = subject.getGraduates(promoId, "EL");

    assertEquals(1, graduates.size());
    assertEquals("s1", graduates.get(0).getStudentId());
  }

  @Test
  void student_with_leave_flow_excluded() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student = aStudent("s1", "Alice", "A");

    GroupFlow joinFlow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(java.time.Instant.parse("2024-01-01T00:00:00Z"))
            .build();
    GroupFlow leaveFlow =
        GroupFlow.builder()
            .id("f2")
            .group(group)
            .student(student)
            .flowType(FlowType.LEAVE)
            .flowDatetime(java.time.Instant.parse("2024-06-01T00:00:00Z"))
            .build();

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(joinFlow, leaveFlow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());

    var graduates = subject.getGraduates(promoId, "EL");

    assertTrue(graduates.isEmpty());
  }

  @Test
  void graduate_rank_is_in_result_order() {
    var promoId = "promo-1";
    var group = aGroup("g1", Path.EL);
    var student1 = aStudent("s1", "Alice", "A");
    var student2 = aStudent("s2", "Bob", "B");
    var student3 = aStudent("s3", "Charlie", "C");
    var course = aCourse("c1", "INF101", 30);
    var assignment = anAssignment("ca1", course, group);
    var exam = anExam("e1", "ca1", 1.0);

    when(groupRepository.findByPromotionId(promoId)).thenReturn(List.of(group));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                aJoinFlow("g1", student1), aJoinFlow("g1", student2), aJoinFlow("g1", student3)));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(assignment));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam));
    when(gradeRepository.findByExamId("e1"))
        .thenReturn(
            List.of(
                aGrade("g1", "e1", "s1", 10.0),
                aGrade("g2", "e1", "s2", 20.0),
                aGrade("g3", "e1", "s3", 15.0)));

    var graduates = subject.getGraduates(promoId, "EL");

    assertEquals(3, graduates.size());
    assertEquals("s2", graduates.get(0).getStudentId());
    assertEquals("s3", graduates.get(1).getStudentId());
    assertEquals("s1", graduates.get(2).getStudentId());
  }
}
