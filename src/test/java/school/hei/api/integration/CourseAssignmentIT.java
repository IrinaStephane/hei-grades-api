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
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.CourseAssignmentCreation;
import school.hei.api.model.dto.CourseAssignmentRest;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;

class CourseAssignmentIT extends FacadeITMockedThirdParties {

  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private PromotionRepository promotionRepository;

  private User admin;
  private User teacher;
  private String adminToken;
  private Promotion promotion;
  private Group group;
  private Course course;
  private CourseAssignment existingAssignment;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "admin-" + randomUUID() + "@hei.school");
    teacher = saveUser(Role.TEACHER, "teacher-" + randomUUID() + "@hei.school");
    adminToken = tokenFor(jwtService, admin);

    promotion =
        promotionRepository.save(
            Promotion.builder().ref("P-" + randomUUID()).entryYear(2025).build());
    group =
        groupRepository.save(
            Group.builder().ref("G-" + randomUUID()).path(Path.EL).promotion(promotion).build());
    course =
        courseRepository.save(
            Course.builder().code("C-" + randomUUID()).title("Algorithmique").credits(2).build());
    existingAssignment =
        courseAssignmentRepository.save(
            CourseAssignment.builder()
                .course(course)
                .teacher(teacher)
                .group(group)
                .year(2025)
                .semester(1)
                .build());
  }

  @Test
  void admin_creates_assignment_ok() {
    var creation = aCreation();
    var response = createAssignment(adminToken, creation);

    assertStatus(HttpStatus.CREATED, response);
    var created = response.getBody();
    assertValidUUID(created.getId());
    assertEquals(course.getId(), created.getCourseId());
    assertEquals(teacher.getId(), created.getTeacherId());
    assertEquals(group.getId(), created.getGroupId());
    assertEquals(2025, created.getYear());
    assertEquals(2, created.getSemester());
  }

  @Test
  void admin_creates_duplicate_assignment_conflicts() {
    var creation = aCreation();
    createAssignment(adminToken, creation);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT,
        "This course/teacher/group is already assigned for year 2025 semester 2",
        response.getBody());
  }

  @Test
  void admin_creates_with_unknown_course_not_found() {
    var creation = aCreationWithCourse(NOT_EXISTING_ID);
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Course " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void admin_creates_with_non_teacher_user_bad_request() {
    var student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");
    var creation = aCreationWithTeacher(student.getId());
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.BAD_REQUEST, response);
    assertRestException(
        HttpStatus.BAD_REQUEST,
        "User " + student.getId() + " is not a teacher",
        response.getBody());
    userRepository.delete(student);
  }

  @Test
  void admin_reads_assignments_ok() {
    var all = getAssignments(adminToken, null, null, null);
    assertStatus(HttpStatus.OK, all);
    assertEquals(1, all.getBody().size());

    var byTeacher = getAssignments(adminToken, teacher.getId(), null, null);
    assertStatus(HttpStatus.OK, byTeacher);
    assertEquals(existingAssignment.getId(), byTeacher.getBody().get(0).getId());

    var byGroup = getAssignments(adminToken, null, group.getId(), null);
    assertStatus(HttpStatus.OK, byGroup);
    assertEquals(existingAssignment.getId(), byGroup.getBody().get(0).getId());

    var byCourse = getAssignments(adminToken, null, null, course.getId());
    assertStatus(HttpStatus.OK, byCourse);
    assertEquals(existingAssignment.getId(), byCourse.getBody().get(0).getId());
  }

  @Test
  void admin_reads_assignment_by_id_ok() {
    var response = getAssignmentById(adminToken, existingAssignment.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(existingAssignment.getId(), response.getBody().getId());
  }

  @Test
  void admin_reads_unknown_assignment_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments/" + NOT_EXISTING_ID),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND,
        "CourseAssignment " + NOT_EXISTING_ID + " not found",
        response.getBody());
  }

  @Test
  void admin_updates_assignment_ok() {
    var update =
        CourseAssignmentCreation.builder()
            .courseId(course.getId())
            .teacherId(teacher.getId())
            .groupId(group.getId())
            .year(2026)
            .semester(1)
            .build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments/" + existingAssignment.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            CourseAssignmentRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(2026, response.getBody().getYear());
  }

  @Test
  void admin_deletes_assignment_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments/" + existingAssignment.getId()),
            HttpMethod.DELETE,
            entity(admin),
            Object.class);
    assertStatus(HttpStatus.NO_CONTENT, response);

    var afterDelete = getAssignmentById(adminToken, existingAssignment.getId());
    assertStatus(HttpStatus.NOT_FOUND, afterDelete);
  }

  @Test
  void admin_deletes_unknown_assignment_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/course_assignments/" + NOT_EXISTING_ID),
            HttpMethod.DELETE,
            entity(admin),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND,
        "CourseAssignment " + NOT_EXISTING_ID + " not found",
        response.getBody());
  }

  private CourseAssignmentCreation aCreation() {
    return aCreationWithCourse(course.getId());
  }

  private CourseAssignmentCreation aCreationWithCourse(String courseId) {
    return CourseAssignmentCreation.builder()
        .courseId(courseId)
        .teacherId(teacher.getId())
        .groupId(group.getId())
        .year(2025)
        .semester(2)
        .build();
  }

  private CourseAssignmentCreation aCreationWithTeacher(String teacherId) {
    return CourseAssignmentCreation.builder()
        .courseId(course.getId())
        .teacherId(teacherId)
        .groupId(group.getId())
        .year(2025)
        .semester(2)
        .build();
  }

  private ResponseEntity<CourseAssignmentRest> createAssignment(
      String token, CourseAssignmentCreation creation) {
    return restTemplate.exchange(
        apiUrl(localPort, "/course_assignments"),
        HttpMethod.POST,
        entity(token, creation),
        CourseAssignmentRest.class);
  }

  private ResponseEntity<CourseAssignmentRest> getAssignmentById(String token, String id) {
    return restTemplate.exchange(
        apiUrl(localPort, "/course_assignments/" + id),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        CourseAssignmentRest.class);
  }

  private ResponseEntity<List<CourseAssignmentRest>> getAssignments(
      String token, String teacherId, String groupId, String courseId) {
    var url = apiUrl(localPort, "/course_assignments");
    if (teacherId != null) {
      url += "?teacherId=" + teacherId;
    } else if (groupId != null) {
      url += "?groupId=" + groupId;
    } else if (courseId != null) {
      url += "?courseId=" + courseId;
    }
    return restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        new ParameterizedTypeReference<List<CourseAssignmentRest>>() {});
  }

  @AfterEach
  void tearDown() {
    courseAssignmentRepository.deleteAll();
    courseRepository.deleteAll();
    groupRepository.deleteAll();
    promotionRepository.deleteAll();
    userRepository.deleteAll(List.of(teacher, admin));
  }
}
