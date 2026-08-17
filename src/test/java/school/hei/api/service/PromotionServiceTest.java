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

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.PromotionCreation;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.UserRepository;

class PromotionServiceTest {

  private final PromotionRepository promotionRepository = mock(PromotionRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final GroupFlowRepository groupFlowRepository = mock(GroupFlowRepository.class);
  private final PromotionService subject =
      new PromotionService(promotionRepository, userRepository, groupFlowRepository);

  private static Promotion aPromotion() {
    return Promotion.builder().id("p1").ref("P1").entryYear(2025).build();
  }

  @Test
  void get_all_ok() {
    var promotions = List.of(aPromotion());
    when(promotionRepository.findAll()).thenReturn(promotions);

    assertEquals(promotions, subject.getAll());
  }

  @Test
  void get_by_id_ok() {
    when(promotionRepository.findById("p1")).thenReturn(Optional.of(aPromotion()));

    assertEquals("p1", subject.getById("p1").getId());
  }

  @Test
  void get_by_unknown_id_not_found() {
    when(promotionRepository.findById("nope")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> subject.getById("nope"));
  }

  @Test
  void create_promotion_ok() {
    var creation = PromotionCreation.builder().ref("P2").entryYear(2026).build();
    when(promotionRepository.existsByRef("P2")).thenReturn(false);
    when(promotionRepository.save(any(Promotion.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var created = subject.create(creation);

    assertEquals("P2", created.getRef());
    assertEquals(2026, created.getEntryYear());
  }

  @Test
  void create_promotion_with_taken_ref_conflicts() {
    var creation = PromotionCreation.builder().ref("P1").entryYear(2026).build();
    when(promotionRepository.existsByRef("P1")).thenReturn(true);

    assertThrows(ConflictException.class, () -> subject.create(creation));
    verify(promotionRepository, never()).save(any());
  }

  @Test
  void update_promotion_ok() {
    var promotion = aPromotion();
    when(promotionRepository.findById("p1")).thenReturn(Optional.of(promotion));
    when(promotionRepository.save(any(Promotion.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var updated = subject.update("p1", PromotionCreation.builder().ref("P9").entryYear(2030).build());

    assertEquals("P9", updated.getRef());
    assertEquals(2030, updated.getEntryYear());
  }

  @Test
  void delete_promotion_ok() {
    when(promotionRepository.existsById("p1")).thenReturn(true);

    subject.delete("p1");

    verify(promotionRepository).deleteById("p1");
  }

  @Test
  void delete_unknown_promotion_not_found() {
    when(promotionRepository.existsById("nope")).thenReturn(false);

    assertThrows(NotFoundException.class, () -> subject.delete("nope"));
  }

  @Test
  void students_of_promotion_are_the_active_ones_in_its_groups() {
    var promotion = aPromotion();
    var otherPromotion = Promotion.builder().id("p2").build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(promotion).build();
    var student = User.builder().id("s1").firstName("A").lastName("B").role(Role.STUDENT).build();
    var joinFlow =
        GroupFlow.builder()
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(now())
            .build();
    when(promotionRepository.findById("p1")).thenReturn(Optional.of(promotion));
    when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(student));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(joinFlow));

    var summaries = subject.getStudents("p1");

    assertEquals(1, summaries.size());
    assertEquals("s1", summaries.get(0).getId());
    assertEquals("g1", summaries.get(0).getCurrentGroup().getId());
  }

  @Test
  void students_who_left_or_never_joined_are_excluded() {
    var promotion = aPromotion();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(promotion).build();
    var leftStudent =
        User.builder().id("s1").firstName("A").lastName("B").role(Role.STUDENT).build();
    var neverJoinedStudent =
        User.builder().id("s2").firstName("C").lastName("D").role(Role.STUDENT).build();
    var leaveFlow =
        GroupFlow.builder()
            .group(group)
            .student(leftStudent)
            .flowType(FlowType.LEAVE)
            .flowDatetime(now())
            .build();
    when(promotionRepository.findById("p1")).thenReturn(Optional.of(promotion));
    when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(leftStudent, neverJoinedStudent));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s1"))
        .thenReturn(Optional.of(leaveFlow));
    when(groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc("s2"))
        .thenReturn(Optional.empty());

    var summaries = subject.getStudents("p1");

    assertTrue(summaries.isEmpty());
  }
}