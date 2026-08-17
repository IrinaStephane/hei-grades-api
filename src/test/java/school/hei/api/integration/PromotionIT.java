package school.hei.api.integration;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.api.integration.conf.ApiAssertions.assertRestException;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.ApiAssertions.assertValidUUID;
import static school.hei.api.integration.conf.TestAuth.tokenFor;
import static school.hei.api.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.hei.api.endpoint.rest.model.RestException;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.PromotionCreation;
import school.hei.api.model.dto.PromotionRest;
import school.hei.api.model.dto.StudentSummaryRest;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;

class PromotionIT extends FacadeITMockedThirdParties {

  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;

  private User admin;
  private User teacher;
  private String adminToken;
  private String teacherToken;
  private String refPrefix;
  private Promotion existingPromotion;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "admin-" + randomUUID() + "@hei.school");
    teacher = saveUser(Role.TEACHER, "teacher-" + randomUUID() + "@hei.school");
    adminToken = tokenFor(jwtService, admin);
    teacherToken = tokenFor(jwtService, teacher);

    refPrefix = "PROMO" + randomUUID().toString().substring(0, 8);
    existingPromotion = promotionRepository.save(aPromotion(refPrefix + "A", 2023));
  }

  @Test
  void admin_creates_promotion_ok() {
    var creation = aPromotionCreation();
    var response = createPromotion(adminToken, creation);

    assertStatus(HttpStatus.CREATED, response);
    var created = response.getBody();
    assertValidUUID(created.getId());
    assertEquals(creation.getRef(), created.getRef());
    assertEquals(creation.getEntryYear(), created.getEntryYear());
  }

  @Test
  void admin_creates_duplicate_ref_conflicts() {
    var creation = aPromotionCreation();
    createPromotion(adminToken, creation);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT, "Promotion " + creation.getRef() + " already exists", response.getBody());
  }

  @Test
  void non_admin_cannot_create_promotion() {
    var response = createPromotion(teacherToken, aPromotionCreation());
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_reads_promotions_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            new ParameterizedTypeReference<List<PromotionRest>>() {});

    assertStatus(HttpStatus.OK, response);
    assertEquals(1, response.getBody().size());
    assertEquals(existingPromotion.getId(), response.getBody().get(0).getId());
  }

  @Test
  void admin_reads_promotion_by_id_ok() {
    var response = getPromotionById(adminToken, existingPromotion.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(existingPromotion.getId(), response.getBody().getId());
  }

  @Test
  void admin_reads_unknown_promotion_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + NOT_EXISTING_ID),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Promotion " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void admin_updates_promotion_ok() {
    var update = PromotionCreation.builder().ref(refPrefix + "B").entryYear(2024).build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + existingPromotion.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            PromotionRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(refPrefix + "B", response.getBody().getRef());
    assertEquals(2024, response.getBody().getEntryYear());
  }

  @Test
  void admin_deletes_promotion_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + existingPromotion.getId()),
            HttpMethod.DELETE,
            entity(admin),
            Object.class);
    assertStatus(HttpStatus.NO_CONTENT, response);

    var afterDelete = getPromotionById(adminToken, existingPromotion.getId());
    assertStatus(HttpStatus.NOT_FOUND, afterDelete);
  }

  @Test
  void admin_reads_promotion_students_ok() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");
    var group = groupRepository.save(aGroup(existingPromotion));
    groupFlowRepository.save(
        GroupFlow.builder()
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(now())
            .build());

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + existingPromotion.getId() + "/students"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(teacher)),
            new ParameterizedTypeReference<List<StudentSummaryRest>>() {});

    assertStatus(HttpStatus.OK, response);
    var summaries = response.getBody();
    assertEquals(1, summaries.size());
    assertEquals(student.getId(), summaries.get(0).getId());
    assertEquals(group.getId(), summaries.get(0).getCurrentGroup().getId());

    groupFlowRepository.deleteAll();
    userRepository.delete(student);
    groupRepository.delete(group);
  }

  @Test
  void student_cannot_read_promotion_students() {
    var student = saveUser(Role.STUDENT, "forbidden-" + randomUUID() + "@hei.school");
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + existingPromotion.getId() + "/students"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(student)),
            RestException.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
    userRepository.delete(student);
  }

  private PromotionCreation aPromotionCreation() {
    return PromotionCreation.builder()
        .ref(refPrefix + randomUUID().toString().substring(0, 4))
        .entryYear(2025)
        .build();
  }

  private static Promotion aPromotion(String ref, int entryYear) {
    return Promotion.builder().id(randomUUID().toString()).ref(ref).entryYear(entryYear).build();
  }

  private static Group aGroup(Promotion promotion) {
    return Group.builder().id(randomUUID().toString()).ref("G1").path(Path.EL).promotion(promotion).build();
  }

  private ResponseEntity<PromotionRest> createPromotion(String token, PromotionCreation creation) {
    return restTemplate.exchange(
        apiUrl(localPort, "/promotions"),
        HttpMethod.POST,
        entity(token, creation),
        PromotionRest.class);
  }

  private ResponseEntity<PromotionRest> getPromotionById(String token, String id) {
    return restTemplate.exchange(
        apiUrl(localPort, "/promotions/" + id),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        PromotionRest.class);
  }

  @AfterEach
  void tearDown() {
    groupFlowRepository.deleteAll();
    groupRepository.deleteAll();
    promotionRepository.deleteAll();
    userRepository.deleteAll(List.of(teacher, admin));
  }
}