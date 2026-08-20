package school.hei.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;

class GraduateServiceTest {

  private static final String PROMO_ID = "promo-1";

  private final PromotionRepository promotionRepository = mock(PromotionRepository.class);
  private final GroupRepository groupRepository = mock(GroupRepository.class);
  private final GroupFlowRepository groupFlowRepository = mock(GroupFlowRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final GradeRepository gradeRepository = mock(GradeRepository.class);
  private final StudentCurriculumService studentCurriculumService =
      new StudentCurriculumService(
          groupFlowRepository, courseAssignmentRepository, examRepository, gradeRepository);
  private final GraduateService subject =
      new GraduateService(
          promotionRepository,
          studentCurriculumService,
          groupRepository,
          groupFlowRepository,
          examRepository,
          gradeRepository);

  private static Group group(String id, Path path) {
    return Group.builder().id(id).ref(id.toUpperCase()).path(path).build();
  }

  private static User student(String id) {
    return User.builder().id(id).firstName("F").lastName(id).build();
  }

  private static Course course(String id, String code, int credits) {
    return Course.builder().id(id).code(code).title("Course " + code).credits(credits).build();
  }

  private static CourseAssignment assignment(String id, Course course, Group group, int year) {
    return CourseAssignment.builder()
        .id(id)
        .course(course)
        .teacher(User.builder().id("teacher-1").build())
        .group(group)
        .year(year)
        .semester(1)
        .build();
  }

  private static GroupFlow flow(
      String id, Group group, String studentId, FlowType type, String at) {
    return GroupFlow.builder()
        .id(id)
        .group(group)
        .student(student(studentId))
        .flowType(type)
        .flowDatetime(Instant.parse(at))
        .build();
  }

  private static Exam exam(String id, String caId) {
    return Exam.builder().id(id).courseAssignmentId(caId).coefficient(1.0).title("Final").build();
  }

  private static Grade grade(String id, String examId, String studentId, double score) {
    return Grade.builder()
        .id(id)
        .examId(examId)
        .studentId(studentId)
        .score(score)
        .isFinal(true)
        .build();
  }

  private void promotion(int entryYear) {
    when(promotionRepository.findById(PROMO_ID))
        .thenReturn(
            Optional.of(
                Promotion.builder().id(PROMO_ID).ref("PROMO").entryYear(entryYear).build()));
  }

  private void curriculum(
      Group g1, Group g2, Group g3, CourseAssignment a1, CourseAssignment a2, CourseAssignment a3) {
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId(g1.getId())).thenReturn(List.of(a1, a2, a3));
    when(courseAssignmentRepository.findByGroupId(g2.getId())).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId(g3.getId())).thenReturn(List.of());
  }

  @Test
  void graduate_who_switched_groups_is_included() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var a2 = assignment("a2", c2, g2, 2023);
    var a3 = assignment("a3", c3, g1, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    curriculum(g1, g2, g3, a1, a2, a3);
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of(a2));
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));

    var s1 = student("s1");
    var s1Flows =
        List.of(
            flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
            flow("f2", g1, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z"),
            flow("f3", g2, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
            flow("f4", g2, "s1", FlowType.LEAVE, "2024-08-31T12:00:00Z"),
            flow("f5", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z"));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(s1Flows);
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z"),
                flow("f5", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2"))
        .thenReturn(
            List.of(
                flow("f3", g2, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f4", g2, "s1", FlowType.LEAVE, "2024-08-31T12:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3")).thenReturn(List.of());
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s1", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s1", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s1", 16.0)));

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertEquals(1, graduates.size());
    var graduate = graduates.get(0);
    assertEquals("s1", graduate.getStudentId());
    assertEquals(3, graduate.getResults().size());
    assertEquals(List.of(2022, 2023, 2024), graduate.getResultYears());
    assertEquals(15.0, graduate.getGeneralAverage(), 0.001);
    assertTrue(s1.getFirstName().equals(graduate.getFirstName()));
  }

  @Test
  void excludes_student_with_incomplete_curriculum() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var e1 = exam("e1", "a1");
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(a1));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId("g3")).thenReturn(List.of());
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));

    var s2 = student("s2");
    var s2Flows = List.of(flow("f1", g1, "s2", FlowType.JOIN, "2022-09-01T08:00:00Z"));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s2")).thenReturn(s2Flows);
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(List.of(flow("f1", g1, "s2", FlowType.JOIN, "2022-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3")).thenReturn(List.of());
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s2", 15.0)));

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertTrue(graduates.isEmpty());
  }

  @Test
  void excludes_student_with_course_below_10() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var a2 = assignment("a2", c2, g1, 2023);
    var a3 = assignment("a3", c3, g1, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    curriculum(g1, g2, g3, a1, a2, a3);
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));

    var s3 = student("s3");
    var s3Flows =
        List.of(
            flow("f1", g1, "s3", FlowType.JOIN, "2022-09-01T08:00:00Z"),
            flow("f2", g1, "s3", FlowType.JOIN, "2023-09-01T08:00:00Z"),
            flow("f3", g1, "s3", FlowType.JOIN, "2024-09-01T08:00:00Z"));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s3")).thenReturn(s3Flows);
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s3", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s3", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f3", g1, "s3", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3")).thenReturn(List.of());
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s3", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s3", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s3", 8.0)));

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertTrue(graduates.isEmpty());
  }

  @Test
  void filters_by_path() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var a2 = assignment("a2", c2, g1, 2023);
    var a3 = assignment("a3", c3, g1, 2024);
    var a1tn = assignment("a1tn", c1, g3, 2022);
    var a2tn = assignment("a2tn", c2, g3, 2023);
    var a3tn = assignment("a3tn", c3, g3, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    var e1tn = exam("e1tn", "a1tn");
    var e2tn = exam("e2tn", "a2tn");
    var e3tn = exam("e3tn", "a3tn");
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(a1, a2, a3));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId("g3")).thenReturn(List.of(a1tn, a2tn, a3tn));
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));
    when(examRepository.findByCourseAssignmentId("a1tn")).thenReturn(List.of(e1tn));
    when(examRepository.findByCourseAssignmentId("a2tn")).thenReturn(List.of(e2tn));
    when(examRepository.findByCourseAssignmentId("a3tn")).thenReturn(List.of(e3tn));

    var s1 = student("s1");
    var s4 = student("s4");
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f3", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s4"))
        .thenReturn(
            List.of(
                flow("f4", g3, "s4", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f5", g3, "s4", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f6", g3, "s4", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f3", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3"))
        .thenReturn(
            List.of(
                flow("f4", g3, "s4", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f5", g3, "s4", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f6", g3, "s4", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s1", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s1", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s1", 16.0)));
    when(gradeRepository.findByExamId("e1tn")).thenReturn(List.of(grade("g4", "e1tn", "s4", 12.0)));
    when(gradeRepository.findByExamId("e2tn")).thenReturn(List.of(grade("g5", "e2tn", "s4", 13.0)));
    when(gradeRepository.findByExamId("e3tn")).thenReturn(List.of(grade("g6", "e3tn", "s4", 11.0)));

    var elGraduates = subject.getGraduates(PROMO_ID, "EL");
    var tnGraduates = subject.getGraduates(PROMO_ID, "TN");
    var allGraduates = subject.getGraduates(PROMO_ID, null);

    assertEquals(1, elGraduates.size());
    assertEquals("s1", elGraduates.get(0).getStudentId());
    assertEquals(1, tnGraduates.size());
    assertEquals("s4", tnGraduates.get(0).getStudentId());
    assertEquals(2, allGraduates.size());
    assertEquals("s1", allGraduates.get(0).getStudentId());
    assertEquals("s4", allGraduates.get(1).getStudentId());
  }

  @Test
  void graduate_is_kept_in_final_path_list_even_after_early_common_core_group_of_other_path() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var a1 = assignment("a1", c1, g3, 2022);
    var a2 = assignment("a2", c2, g1, 2023);
    var a3 = assignment("a3", c3, g1, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(a2, a3));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId("g3")).thenReturn(List.of(a1));
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));

    var s1 = student("s1");
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                flow("f1", g3, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g3, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z"),
                flow("f3", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f4", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f3", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f4", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3"))
        .thenReturn(
            List.of(
                flow("f1", g3, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g3, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z")));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s1", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s1", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s1", 16.0)));

    var elGraduates = subject.getGraduates(PROMO_ID, "EL");
    var tnGraduates = subject.getGraduates(PROMO_ID, "TN");
    var allGraduates = subject.getGraduates(PROMO_ID, null);

    assertEquals(1, elGraduates.size());
    assertEquals("s1", elGraduates.get(0).getStudentId());
    assertEquals(3, elGraduates.get(0).getResults().size());
    assertTrue(tnGraduates.isEmpty());
    assertEquals(1, allGraduates.size());
    assertEquals("s1", allGraduates.get(0).getStudentId());
  }

  @Test
  void keeps_grades_from_both_groups_when_student_changes_group_mid_year() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var c4 = course("c4", "C4", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var a2 = assignment("a2", c2, g3, 2022);
    var a3 = assignment("a3", c3, g1, 2023);
    var a4 = assignment("a4", c4, g1, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    var e4 = exam("e4", "a4");
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(a1, a3, a4));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId("g3")).thenReturn(List.of(a2));
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));
    when(examRepository.findByCourseAssignmentId("a4")).thenReturn(List.of(e4));

    var s1 = student("s1");
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-01-15T08:00:00Z"),
                flow("f3", g3, "s1", FlowType.JOIN, "2023-01-16T08:00:00Z"),
                flow("f4", g3, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z"),
                flow("f5", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f6", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-01-15T08:00:00Z"),
                flow("f5", g1, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f6", g1, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3"))
        .thenReturn(
            List.of(
                flow("f3", g3, "s1", FlowType.JOIN, "2023-01-16T08:00:00Z"),
                flow("f4", g3, "s1", FlowType.LEAVE, "2023-08-31T12:00:00Z")));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade("g1", "e1", "s1", 15.0)));
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s1", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s1", 16.0)));
    when(gradeRepository.findByExamId("e4")).thenReturn(List.of(grade("g4", "e4", "s1", 15.0)));

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertEquals(1, graduates.size());
    var graduate = graduates.get(0);
    assertEquals("s1", graduate.getStudentId());
    assertEquals(4, graduate.getResults().size());
    assertEquals(List.of(2022, 2023, 2024), graduate.getResultYears());
    assertEquals(15.0, graduate.getGeneralAverage(), 0.001);
  }

  @Test
  void rejects_invalid_path() {
    promotion(2022);
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of());

    assertThrows(BadRequestException.class, () -> subject.getGraduates(PROMO_ID, "XYZ"));
  }

  @Test
  void deduplicates_shared_course_across_groups_keeping_the_graded_one() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var c2 = course("c2", "C2", 30);
    var c3 = course("c3", "C3", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    var a2 = assignment("a2", c1, g3, 2022);
    var a3 = assignment("a3", c2, g3, 2023);
    var a4 = assignment("a4", c3, g3, 2024);
    var e1 = exam("e1", "a1");
    var e2 = exam("e2", "a2");
    var e3 = exam("e3", "a3");
    var e4 = exam("e4", "a4");
    when(groupRepository.findByPromotionId(PROMO_ID)).thenReturn(List.of(g1, g2, g3));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(a1));
    when(courseAssignmentRepository.findByGroupId("g2")).thenReturn(List.of());
    when(courseAssignmentRepository.findByGroupId("g3")).thenReturn(List.of(a2, a3, a4));
    when(examRepository.findByCourseAssignmentId("a1")).thenReturn(List.of(e1));
    when(examRepository.findByCourseAssignmentId("a2")).thenReturn(List.of(e2));
    when(examRepository.findByCourseAssignmentId("a3")).thenReturn(List.of(e3));
    when(examRepository.findByCourseAssignmentId("a4")).thenReturn(List.of(e4));

    var s1 = student("s1");
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-01-15T08:00:00Z"),
                flow("f3", g3, "s1", FlowType.JOIN, "2023-01-16T08:00:00Z"),
                flow("f4", g3, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f5", g3, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-01-15T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3"))
        .thenReturn(
            List.of(
                flow("f3", g3, "s1", FlowType.JOIN, "2023-01-16T08:00:00Z"),
                flow("f4", g3, "s1", FlowType.JOIN, "2023-09-01T08:00:00Z"),
                flow("f5", g3, "s1", FlowType.JOIN, "2024-09-01T08:00:00Z")));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of());
    when(gradeRepository.findByExamId("e2")).thenReturn(List.of(grade("g2", "e2", "s1", 14.0)));
    when(gradeRepository.findByExamId("e3")).thenReturn(List.of(grade("g3", "e3", "s1", 15.0)));
    when(gradeRepository.findByExamId("e4")).thenReturn(List.of(grade("g4", "e4", "s1", 16.0)));

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertEquals(1, graduates.size());
    var graduate = graduates.get(0);
    assertEquals(3, graduate.getResults().size());
    assertEquals(
        1, graduate.getResults().stream().filter(r -> r.getCourseId().equals("c1")).count());
    var c1Result =
        graduate.getResults().stream()
            .filter(r -> r.getCourseId().equals("c1"))
            .findFirst()
            .orElseThrow();
    assertEquals(14.0, c1Result.getFinalGrade(), 0.001);
    assertEquals(List.of(2022, 2023, 2024), graduate.getResultYears());
  }

  @Test
  void excludes_student_whose_latest_flow_is_leave() {
    promotion(2022);
    var g1 = group("g1", Path.EL);
    var g2 = group("g2", Path.EL);
    var g3 = group("g3", Path.TN);
    var c1 = course("c1", "C1", 30);
    var a1 = assignment("a1", c1, g1, 2022);
    curriculum(g1, g2, g3, a1, a1, a1);

    var s1 = student("s1");
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g1"))
        .thenReturn(
            List.of(
                flow("f1", g1, "s1", FlowType.JOIN, "2022-09-01T08:00:00Z"),
                flow("f2", g1, "s1", FlowType.LEAVE, "2023-06-01T08:00:00Z")));
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g2")).thenReturn(List.of());
    when(groupFlowRepository.findByGroupIdOrderByFlowDatetimeAsc("g3")).thenReturn(List.of());

    var graduates = subject.getGraduates(PROMO_ID, null);

    assertTrue(graduates.isEmpty());
  }
}
