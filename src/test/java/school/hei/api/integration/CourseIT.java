package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import school.hei.api.model.User;
import school.hei.api.model.dto.CourseCreation;
import school.hei.api.model.dto.CourseRest;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseRepository;

class CourseIT extends FacadeITMockedThirdParties {

  @Autowired private CourseRepository courseRepository;

  private String codePrefix;
  private User admin;
  private User student;
  private String adminToken;
  private String studentToken;
  private Course existingCourse;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "admin-" + randomUUID() + "@hei.school");
    student = saveUser(Role.STUDENT, "student-" + randomUUID() + "@hei.school");
    adminToken = tokenFor(jwtService, admin);
    studentToken = tokenFor(jwtService, student);

    codePrefix = "CIT" + randomUUID().toString().substring(0, 8);
    existingCourse = courseRepository.save(aCourse(codePrefix + "A", "Algorithmique", 2));
  }

  @Test
  void admin_creates_course_ok() {
    var creation = aCourseCreation();
    var response = createCourse(adminToken, creation);

    assertStatus(HttpStatus.CREATED, response);
    var created = response.getBody();
    assertValidUUID(created.getId());
    assertEquals(creation.getCode(), created.getCode());
    assertEquals("Nouveau cours", created.getTitle());
    assertEquals(4, created.getCredits());
  }

  @Test
  void admin_creates_duplicate_code_conflicts() {
    var creation = aCourseCreation();
    createCourse(adminToken, creation);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses"),
            HttpMethod.POST,
            entity(adminToken, creation),
            RestException.class);
    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT,
        "Course " + creation.getCode() + " already exists",
        response.getBody());
  }

  @Test
  void non_admin_cannot_create_course() {
    var response = createCourse(studentToken, aCourseCreation());
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_creates_invalid_course_is_bad_request() {
    var response = createCourse(adminToken, CourseCreation.builder().build());
    assertStatus(HttpStatus.BAD_REQUEST, response);
  }

  @Test
  void admin_reads_courses_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            new ParameterizedTypeReference<List<CourseRest>>() {});

    assertStatus(HttpStatus.OK, response);
    assertTrue(response.getBody().stream().anyMatch(c -> c.getId().equals(existingCourse.getId())));
  }

  @Test
  void admin_reads_course_by_id_ok() {
    var response = getCourseById(adminToken, existingCourse.getId());
    assertStatus(HttpStatus.OK, response);
    assertEquals(existingCourse.getId(), response.getBody().getId());
    assertEquals(existingCourse.getCode(), response.getBody().getCode());
  }

  @Test
  void admin_reads_unknown_course_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses/" + NOT_EXISTING_ID),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Course " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  @Test
  void admin_updates_course_ok() {
    var update = CourseCreation.builder().code(codePrefix + "B").title("Bases").credits(6).build();
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses/" + existingCourse.getId()),
            HttpMethod.PUT,
            entity(admin, update),
            CourseRest.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(codePrefix + "B", response.getBody().getCode());
    assertEquals("Bases", response.getBody().getTitle());
    assertEquals(6, response.getBody().getCredits());
  }

  @Test
  void admin_deletes_course_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses/" + existingCourse.getId()),
            HttpMethod.DELETE,
            entity(admin),
            Object.class);
    assertStatus(HttpStatus.NO_CONTENT, response);

    var afterDelete = getCourseById(adminToken, existingCourse.getId());
    assertStatus(HttpStatus.NOT_FOUND, afterDelete);
  }

  @Test
  void admin_deletes_unknown_course_not_found() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/courses/" + NOT_EXISTING_ID),
            HttpMethod.DELETE,
            entity(admin),
            RestException.class);
    assertStatus(HttpStatus.NOT_FOUND, response);
    assertRestException(
        HttpStatus.NOT_FOUND, "Course " + NOT_EXISTING_ID + " not found", response.getBody());
  }

  private CourseCreation aCourseCreation() {
    return CourseCreation.builder()
        .code(codePrefix + randomUUID().toString().substring(0, 4))
        .title("Nouveau cours")
        .credits(4)
        .build();
  }

  private static Course aCourse(String code, String title, int credits) {
    return Course.builder()
        .id(randomUUID().toString())
        .code(code)
        .title(title)
        .credits(credits)
        .build();
  }

  private ResponseEntity<CourseRest> createCourse(String token, CourseCreation creation) {
    return restTemplate.exchange(
        apiUrl(localPort, "/courses"), HttpMethod.POST, entity(token, creation), CourseRest.class);
  }

  private ResponseEntity<CourseRest> getCourseById(String token, String id) {
    return restTemplate.exchange(
        apiUrl(localPort, "/courses/" + id),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        CourseRest.class);
  }

  @AfterEach
  void tearDown() {
    courseRepository.deleteAll(
        courseRepository.findAll().stream()
            .filter(c -> c.getCode() != null && c.getCode().startsWith(codePrefix))
            .toList());
    userRepository.deleteAll(List.of(student, admin));
  }
}
