package school.hei.api.integration;

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
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.GroupCreation;
import school.hei.api.model.dto.GroupFlowCreation;
import school.hei.api.model.dto.GroupFlowRest;
import school.hei.api.model.dto.GroupRest;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;

class GroupIT extends FacadeITMockedThirdParties {

  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private PromotionRepository promotionRepository;

  private User admin;
  private String adminToken;
  private String refPrefix;
  private Promotion promotion;
  private Group existingGroup;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "admin-" + randomUUID() + "@hei.school");
    adminToken = tokenFor(jwtService, admin);

    refPrefix = "GRP" + randomUUID().toString().substring(0, 8);
    promotion =
        promotionRepository.save(Promotion.builder().ref("P-" + refPrefix).entryYear(2025).build());
    existingGroup = groupRepository.save(aGroup(refPrefix + "A", Path.EL, promotion));
  }

  @Test
  void admin_creates_group_ok() {
    var creation = aGroupCreation();
    var response = createGroup(adminToken, creation);

    assertStatus(HttpStatus.CREATED, response);
    var created = response.getBody();
    assertValidUUID(created.getId());
    assertEquals(creation.getRef(), created.getRef());
    assertEquals(creation.getPath(), created.getPath());
    assertEquals(promotion.getId(), created.getPromotionId());
  }

  @Test
  void non_admin_cannot_create_group() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");
    var response = createGroup(tokenFor(jwtService, student), aGroupCreation());
    assertStatus(HttpStatus.FORBIDDEN, response);
    userRepository.delete(student);
  }

  @Test
  void admin_creates_group_with_unknown_promotion_not_found() {
    var creation =
        GroupCreation.builder()
            .ref(refPrefix + "X")
            .path(Path.TN)
            .promotionId(NOT_EXISTING_ID)
            .build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/groups"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Promotion " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void admin_reads_groups_ok() {
    var response = getGroups(adminToken, null, null);
    assertStatus(HttpStatus.OK, response);
    assertEquals(1, response.getBody().size());
    assertEquals(existingGroup.getId(), response.getBody().get(0).getId());
  }

  @Test
  void admin_reads_groups_filtered_by_promotion_ok() {
    var response = getGroups(adminToken, promotion.getId(), null);
    assertStatus(HttpStatus.OK, response);
    assertEquals(1, response.getBody().size());

    var noMatch = getGroups(adminToken, NOT_EXISTING_ID, null);
    assertStatus(HttpStatus.OK, noMatch);
    assertEquals(0, noMatch.getBody().size());
  }

  @Test
  void admin_reads_groups_filtered_by_path_ok() {
    var response = getGroups(adminToken, null, Path.TN);
    assertStatus(HttpStatus.OK, response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void admin_reads_group_by_id_ok() {
    var response = getGroupById(adminToken, existingGroup.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(existingGroup.getId(), response.getBody().getId());
  }

  @Test
  void admin_reads_unknown_group_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/groups/" + NOT_EXISTING_ID),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Group " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void admin_updates_group_ok() {
    var update =
        GroupCreation.builder()
            .ref(refPrefix + "B")
            .path(Path.TN)
            .promotionId(promotion.getId())
            .build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/groups/" + existingGroup.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            GroupRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(refPrefix + "B", response.getBody().getRef());
    assertEquals(Path.TN, response.getBody().getPath());
  }

  @Test
  void admin_deletes_group_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/groups/" + existingGroup.getId()),
            HttpMethod.DELETE,
            entity(admin),
            Object.class);
    assertStatus(HttpStatus.NO_CONTENT, response);

    var afterDelete = getGroupById(adminToken, existingGroup.getId());
    assertStatus(HttpStatus.NOT_FOUND, afterDelete);
  }

  @Test
  void student_joins_then_leaves_group_ok() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");

    var join = recordFlow(adminToken, existingGroup.getId(), student, FlowType.JOIN);
    assertStatus(HttpStatus.CREATED, join);
    assertEquals(FlowType.JOIN, join.getBody().getFlowType());

    var leave = recordFlow(adminToken, existingGroup.getId(), student, FlowType.LEAVE);
    assertStatus(HttpStatus.CREATED, leave);
    assertEquals(FlowType.LEAVE, leave.getBody().getFlowType());

    var history = getGroupFlows(adminToken, student.getId());
    assertStatus(HttpStatus.OK, history);
    assertEquals(2, history.getBody().size());

    groupFlowRepository.deleteAll();
    userRepository.delete(student);
  }

  @Test
  void student_joins_twice_is_bad_request() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");
    recordFlow(adminToken, existingGroup.getId(), student, FlowType.JOIN);

    var secondJoin =
        recordFlow(adminToken, existingGroup.getId(), student, FlowType.JOIN, RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, secondJoin);
    assertRestException(
        HttpStatus.BAD_REQUEST,
        "Student " + student.getId() + " is already active in a group; LEAVE it first",
        secondJoin.getBody());

    groupFlowRepository.deleteAll();
    userRepository.delete(student);
  }

  @Test
  void student_leaves_without_joining_is_bad_request() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");

    var leave =
        recordFlow(adminToken, existingGroup.getId(), student, FlowType.LEAVE, RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, leave);
    assertRestException(
        HttpStatus.BAD_REQUEST,
        "Student " + student.getId() + " is not currently active in any group",
        leave.getBody());

    userRepository.delete(student);
  }

  @Test
  void flow_for_non_student_is_bad_request() {
    var teacher = saveUser(Role.TEACHER, "teacher-" + randomUUID() + "@hei.school");
    var response =
        recordFlow(adminToken, existingGroup.getId(), teacher, FlowType.JOIN, RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, response);
    assertRestException(
        HttpStatus.BAD_REQUEST,
        "User " + teacher.getId() + " is not a student",
        response.getBody());
    userRepository.delete(teacher);
  }

  private GroupCreation aGroupCreation() {
    return GroupCreation.builder()
        .ref(refPrefix + randomUUID().toString().substring(0, 4))
        .path(Path.EL)
        .promotionId(promotion.getId())
        .build();
  }

  private static Group aGroup(String ref, Path path, Promotion promotion) {
    return Group.builder()
        .id(randomUUID().toString())
        .ref(ref)
        .path(path)
        .promotion(promotion)
        .build();
  }

  private ResponseEntity<GroupRest> createGroup(String token, GroupCreation creation) {
    return restTemplate.exchange(
        apiUrl(localPort, "/groups"), HttpMethod.POST, entity(token, creation), GroupRest.class);
  }

  private ResponseEntity<GroupRest> getGroupById(String token, String id) {
    return restTemplate.exchange(
        apiUrl(localPort, "/groups/" + id),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        GroupRest.class);
  }

  private ResponseEntity<List<GroupRest>> getGroups(String token, String promotionId, Path path) {
    var url = apiUrl(localPort, "/groups");
    if (promotionId != null) {
      url += "?promotionId=" + promotionId;
    } else if (path != null) {
      url += "?path=" + path;
    }
    return restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        new ParameterizedTypeReference<List<GroupRest>>() {});
  }

  private ResponseEntity<GroupFlowRest> recordFlow(
      String token, String groupId, User student, FlowType flowType) {
    return recordFlow(token, groupId, student, flowType, GroupFlowRest.class);
  }

  private <T> ResponseEntity<T> recordFlow(
      String token, String groupId, User student, FlowType flowType, Class<T> responseType) {
    var creation =
        GroupFlowCreation.builder().studentId(student.getId()).flowType(flowType).build();
    return restTemplate.exchange(
        apiUrl(localPort, "/groups/" + groupId + "/flows"),
        HttpMethod.POST,
        entity(token, creation),
        responseType);
  }

  private ResponseEntity<List<GroupFlowRest>> getGroupFlows(String token, String studentId) {
    return restTemplate.exchange(
        apiUrl(localPort, "/users/" + studentId + "/group_flows"),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        new ParameterizedTypeReference<List<GroupFlowRest>>() {});
  }

  @AfterEach
  void tearDown() {
    groupFlowRepository.deleteAll();
    groupRepository.deleteAll();
    promotionRepository.deleteAll();
    userRepository.deleteAll(List.of(admin));
  }
}
