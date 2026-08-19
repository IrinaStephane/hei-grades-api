package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.api.integration.conf.ApiAssertions.assertRestException;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import school.hei.api.endpoint.rest.model.GradeCreationRequest;
import school.hei.api.endpoint.rest.model.GradeUpdateRequest;
import school.hei.api.endpoint.rest.model.RestException;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;

class GradeIT extends FacadeITMockedThirdParties {

  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private GradeHistoryRepository gradeHistoryRepository;

  private User admin;
  private User teacher;
  private User student;
  private Promotion promotion;
  private Group group;
  private Course course;
  private CourseAssignment courseAssignment;
  private school.hei.api.repository.model.Exam exam;
  private school.hei.api.repository.model.Grade grade;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "grade-admin-" + randomUUID() + "@hei.school");
    teacher = saveUser(Role.TEACHER, "grade-teacher-" + randomUUID() + "@hei.school");
    student = saveUser(Role.STUDENT, "grade-student-" + randomUUID() + "@hei.school");

    promotion =
        promotionRepository.save(
            Promotion.builder().ref("PROMO-" + randomUUID()).entryYear(2024).build());
    group =
        groupRepository.save(
            Group.builder().ref("K1-" + randomUUID()).path(Path.EL).promotion(promotion).build());
    course =
        courseRepository.save(
            Course.builder().code("CODE-" + randomUUID()).title("Course").credits(30).build());
    courseAssignment =
        courseAssignmentRepository.save(
            CourseAssignment.builder()
                .course(course)
                .teacher(teacher)
                .group(group)
                .year(2024)
                .semester(1)
                .build());
    exam =
        examRepository.save(
            school.hei.api.repository.model.Exam.builder()
                .id(randomUUID().toString())
                .courseAssignmentId(courseAssignment.getId())
                .title("Exam")
                .examinationDate(Instant.now())
                .coefficient(1.0)
                .build());
    grade =
        gradeRepository.save(
            school.hei.api.repository.model.Grade.builder()
                .id(randomUUID().toString())
                .examId(exam.getId())
                .studentId(student.getId())
                .score(14.0)
                .isFinal(false)
                .build());
  }

  @Test
  void admin_creates_grade_ok() {
    var teacher2 = saveUser(Role.TEACHER, "grade-teacher2-" + randomUUID() + "@hei.school");
    var ca2 =
        courseAssignmentRepository.save(
            CourseAssignment.builder()
                .course(course)
                .teacher(teacher2)
                .group(group)
                .year(2024)
                .semester(2)
                .build());
    var exam2 =
        examRepository.save(
            school.hei.api.repository.model.Exam.builder()
                .id(randomUUID().toString())
                .courseAssignmentId(ca2.getId())
                .title("Exam2")
                .examinationDate(Instant.now())
                .coefficient(0.5)
                .build());
    var body = new GradeCreationRequest(exam2.getId(), student.getId(), 16.0, true);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades"),
            HttpMethod.POST,
            entity(admin, body),
            school.hei.api.repository.model.Grade.class);

    assertStatus(HttpStatus.CREATED, response);
    assertNotNull(response.getBody().getId());
    assertEquals(16.0, response.getBody().getScore());

    gradeRepository.deleteById(response.getBody().getId());
    examRepository.deleteById(exam2.getId());
    courseAssignmentRepository.deleteById(ca2.getId());
    userRepository.delete(teacher2);
  }

  @Test
  void admin_creates_grade_with_unknown_exam_not_found() {
    var body = new GradeCreationRequest(NOT_EXISTING_ID, student.getId(), 15.0, false);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades"),
            HttpMethod.POST,
            entity(admin, body),
            RestException.class);

    assertStatus(HttpStatus.NOT_FOUND, response);
  }

  @Test
  void admin_creates_grade_with_unknown_student_bad_request() {
    var body = new GradeCreationRequest(exam.getId(), NOT_EXISTING_ID, 15.0, false);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades"),
            HttpMethod.POST,
            entity(admin, body),
            RestException.class);

    assertStatus(HttpStatus.BAD_REQUEST, response);
  }

  @Test
  void admin_creates_grade_duplicate_conflict() {
    var body = new GradeCreationRequest(exam.getId(), student.getId(), 15.0, false);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades"),
            HttpMethod.POST,
            entity(admin, body),
            RestException.class);

    assertStatus(HttpStatus.CONFLICT, response);
    assertRestException(
        HttpStatus.CONFLICT,
        "A grade already exists for exam " + exam.getId() + " and student " + student.getId(),
        response.getBody());
  }

  @Test
  void student_cannot_create_grade() {
    var body = new GradeCreationRequest(exam.getId(), student.getId(), 15.0, false);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades"),
            HttpMethod.POST,
            entity(student, body),
            RestException.class);

    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_updates_grade_ok() {
    var body = new GradeUpdateRequest(18.0, "Improved", true);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades/" + grade.getId()),
            HttpMethod.PUT,
            entity(admin, body),
            school.hei.api.repository.model.Grade.class);

    assertStatus(HttpStatus.OK, response);
    assertEquals(18.0, response.getBody().getScore());
    assertTrue(response.getBody().isFinal());
  }

  @Test
  void admin_get_grade_history_ok() {
    var body = new GradeUpdateRequest(18.0, "First change", true);
    restTemplate.exchange(
        apiUrl(localPort, "/api/grades/" + grade.getId()),
        HttpMethod.PUT,
        entity(admin, body),
        school.hei.api.repository.model.Grade.class);

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades/" + grade.getId() + "/history"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            new ParameterizedTypeReference<
                List<school.hei.api.repository.model.GradeHistory>>() {});

    assertStatus(HttpStatus.OK, response);
    assertTrue(response.getBody().size() >= 1);
  }

  @AfterEach
  void tearDown() {
    gradeRepository.delete(grade);
    examRepository.delete(exam);
    courseAssignmentRepository.delete(courseAssignment);
    courseRepository.delete(course);
    groupRepository.delete(group);
    promotionRepository.delete(promotion);
    userRepository.delete(student);
    userRepository.delete(teacher);
    userRepository.delete(admin);
  }
}
