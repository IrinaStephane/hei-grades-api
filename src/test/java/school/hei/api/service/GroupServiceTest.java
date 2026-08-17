package school.hei.api.service;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.GroupCreation;
import school.hei.api.model.dto.GroupFlowCreation;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.UserRepository;

class GroupServiceTest {

  private final GroupRepository groupRepository = mock(GroupRepository.class);
  private final GroupFlowRepository groupFlowRepository = mock(GroupFlowRepository.class);
  private final PromotionRepository promotionRepository = mock(PromotionRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final GroupService subject =
      new GroupService(
          groupRepository,
          groupFlowRepository,
          promotionRepository,
          userRepository,
          courseAssignmentRepository);

  private static Promotion aPromotion() {
    return Promotion.builder().id("p1").ref("P1").entryYear(2025).build();
  }

  private static Group aGroup() {
    return Group.builder().id("g1").ref("G1").path(Path.EL).promotion(aPromotion()).build();
  }

  private static User aStudent() {
    return User.builder().id("s1").firstName("A").lastName("B").role(Role.STUDENT).build();
  }

  @Test
  void get_all_with_no_filter_returns_everything() {
    when(groupRepository.findAll()).thenReturn(List.of(aGroup()));

    assertEquals(1, subject.getAll(null, null).size());
  }

  @Test
  void get_all_filters_by_promotion_and_path() {
    when(groupRepository.findByPromotionIdAndPath("p1", Path.EL)).thenReturn(List.of(aGroup()));
    when(groupRepository.findByPromotionId("p1")).thenReturn(List.of(aGroup()));
    when(groupRepository.findByPath(Path.EL)).thenReturn(List.of(aGroup()));

    assertEquals(1, subject.getAll("p1", Path.EL).size());
    assertEquals(1, subject.getAll("p1", null).size());
    assertEquals(1, subject.getAll(null, Path.EL).size());
  }

  @Test
  void get_by_id_ok() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));

    assertEquals("g1", subject.getById("g1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(groupRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_group_ok() {
    when(promotionRepository.findById("p1")).thenReturn(Optional.of(aPromotion()));
    when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create(GroupCreation.builder().ref("G2").path(Path.EL).promotionId("p1").build());

    assertEquals("G2", created.getRef());
    assertEquals(Path.EL, created.getPath());
    assertEquals("p1", created.getPromotion().getId());
  }

  @Test
  void create_group_with_unknown_promotion_not_found() {
    when(promotionRepository.findById("p1")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> subject.create(GroupCreation.builder().ref("G2").path(Path.EL).promotionId("p1").build()));
    verify(groupRepository, never()).save(any());
  }

  @Test
  void update_group_ok() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(promotionRepository.findById("p2"))
        .thenReturn(Optional.of(Promotion.builder().id("p2").build()));
    when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var updated =
        subject.update("g1", GroupCreation.builder().ref("G9").path(Path.TN).promotionId("p2").build());

    assertEquals("G9", updated.getRef());
    assertEquals(Path.TN, updated.getPath());
    assertEquals("p2", updated.getPromotion().getId());
  }

  @Test
  void delete_group_ok() {
    when(groupRepository.existsById("g1")).thenReturn(true);

    subject.delete("g1");

    verify(groupRepository).deleteById("g1");
  }

  @Test
  void delete_unknown_group_not_found() {
    when(groupRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
  }

  @Test
  void record_join_flow_ok() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.empty());
    when(groupFlowRepository.save(any(GroupFlow.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var flow =
        subject.recordFlow(
            "g1", GroupFlowCreation.builder().studentId("s1").flowType(FlowType.JOIN).build());

    assertEquals(FlowType.JOIN, flow.getFlowType());
    assertEquals("g1", flow.getGroup().getId());
    assertEquals("s1", flow.getStudent().getId());
  }

  @Test
  void record_join_flow_while_active_bad_request() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(GroupFlow.builder().flowType(FlowType.JOIN).build()));

    assertThrows(
        BadRequestException.class,
        () ->
            subject.recordFlow(
                "g1", GroupFlowCreation.builder().studentId("s1").flowType(FlowType.JOIN).build()));
  }

  @Test
  void record_leave_flow_ok() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(GroupFlow.builder().flowType(FlowType.JOIN).build()));
    when(groupFlowRepository.save(any(GroupFlow.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var flow =
        subject.recordFlow(
            "g1", GroupFlowCreation.builder().studentId("s1").flowType(FlowType.LEAVE).build());

    assertEquals(FlowType.LEAVE, flow.getFlowType());
  }

  @Test
  void record_leave_flow_while_inactive_bad_request() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.empty());

    assertThrows(
        BadRequestException.class,
        () ->
            subject.recordFlow(
                "g1", GroupFlowCreation.builder().studentId("s1").flowType(FlowType.LEAVE).build()));
  }

  @Test
  void record_flow_with_unknown_student_not_found() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () ->
            subject.recordFlow(
                "g1", GroupFlowCreation.builder().studentId("nope").flowType(FlowType.JOIN).build()));
  }

  @Test
  void record_flow_with_non_student_bad_request() {
    when(groupRepository.findById("g1")).thenReturn(Optional.of(aGroup()));
    when(userRepository.findById("t1"))
        .thenReturn(Optional.of(User.builder().id("t1").role(Role.TEACHER).build()));

    assertThrows(
        BadRequestException.class,
        () ->
            subject.recordFlow(
                "g1", GroupFlowCreation.builder().studentId("t1").flowType(FlowType.JOIN).build()));
  }

  @Test
  void get_group_flow_history_ok() {
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(List.of(GroupFlow.builder().flowType(FlowType.JOIN).build()));

    assertEquals(1, subject.getGroupFlowHistory("s1").size());
  }

  @Test
  void get_current_group_ok() {
    var group = aGroup();
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(GroupFlow.builder().group(group).flowType(FlowType.JOIN).build()));

    assertEquals("g1", subject.getCurrentGroup("s1").orElseThrow().getId());
  }

  @Test
  void get_current_group_is_empty_after_leave() {
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(GroupFlow.builder().flowType(FlowType.LEAVE).build()));

    assertTrue(subject.getCurrentGroup("s1").isEmpty());
  }

  @Test
  void get_current_group_unknown_student_not_found() {
    when(userRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getCurrentGroup("nope"));
  }

  @Test
  void course_assignments_followed_cover_active_periods() {
    var group1 = Group.builder().id("g1").ref("G1").path(Path.EL).build();
    var group2 = Group.builder().id("g2").ref("G2").path(Path.EL).build();
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                GroupFlow.builder()
                    .group(group1)
                    .flowType(FlowType.JOIN)
                    .flowDatetime(LocalDate.of(2025, 3, 1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                    .build(),
                GroupFlow.builder()
                    .group(group1)
                    .flowType(FlowType.LEAVE)
                    .flowDatetime(LocalDate.of(2025, 5, 15).atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                    .build(),
                GroupFlow.builder()
                    .group(group2)
                    .flowType(FlowType.JOIN)
                    .flowDatetime(LocalDate.of(2025, 8, 1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                    .build()));
    var s1Assignment =
        CourseAssignment.builder()
            .id("a1")
            .course(Course.builder().id("c1").build())
            .group(group1)
            .year(2025)
            .semester(1)
            .build();
    var s2Assignment =
        CourseAssignment.builder()
            .id("a2")
            .course(Course.builder().id("c2").build())
            .group(group2)
            .year(2025)
            .semester(2)
            .build();
    var outOfPeriod =
        CourseAssignment.builder()
            .id("a3")
            .course(Course.builder().id("c3").build())
            .group(group2)
            .year(2024)
            .semester(1)
            .build();
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(s1Assignment));
    when(courseAssignmentRepository.findByGroupId("g2"))
        .thenReturn(List.of(s2Assignment, outOfPeriod));

    var result = subject.getCourseAssignmentsFollowedByStudent("s1");

    assertEquals(2, result.size());
    assertEquals(List.of("a1", "a2"), result.stream().map(CourseAssignment::getId).toList());
  }

  @Test
  void course_assignments_include_still_active_group() {
    var group = aGroup();
    when(userRepository.findById("s1")).thenReturn(Optional.of(aStudent()));
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1"))
        .thenReturn(
            List.of(
                GroupFlow.builder()
                    .group(group)
                    .flowType(FlowType.JOIN)
                    .flowDatetime(now())
                    .build()));
    when(courseAssignmentRepository.findByGroupId("g1"))
        .thenReturn(
            List.of(
                CourseAssignment.builder()
                    .id("a1")
                    .group(group)
                    .year(2026)
                    .semester(2)
                    .build()));

    assertEquals(1, subject.getCourseAssignmentsFollowedByStudent("s1").size());
  }
}
