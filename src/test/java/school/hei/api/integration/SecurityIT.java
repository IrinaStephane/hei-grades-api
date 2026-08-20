package school.hei.api.integration;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestAuth.tokenFor;
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
import school.hei.api.endpoint.rest.model.ExamCreationRequest;
import school.hei.api.endpoint.rest.model.GradeUpdateRequest;
import school.hei.api.endpoint.rest.model.RestException;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.StudentSummaryRest;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeHistoryRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;

class SecurityIT extends FacadeITMockedThirdParties {

  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private GradeHistoryRepository gradeHistoryRepository;

  private User admin;
  private User teacher;
  private User otherTeacher;
  private User student;
  private User otherStudent;
  private String adminToken;
  private String teacherToken;
  private String otherTeacherToken;
  private String studentToken;
  private String otherStudentToken;
  private Promotion promotion;
  private Group group;
  private Course course;
  private CourseAssignment assignment;
  private Exam exam;
  private Exam otherExam;
  private Grade grade;
  private Grade otherGrade;
  private final List<Exam> extraExams = new java.util.ArrayList<>();

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "sec-admin@hei.school");
    teacher = saveUser(Role.TEACHER, "sec-teacher@hei.school");
    otherTeacher = saveUser(Role.TEACHER, "sec-teacher-2@hei.school");
    student = saveUser(Role.STUDENT, "sec-student@hei.school");
    otherStudent = saveUser(Role.STUDENT, "sec-student-2@hei.school");
    adminToken = tokenFor(jwtService, admin);
    teacherToken = tokenFor(jwtService, teacher);
    otherTeacherToken = tokenFor(jwtService, otherTeacher);
    studentToken = tokenFor(jwtService, student);
    otherStudentToken = tokenFor(jwtService, otherStudent);

    var suffix = randomUUID().toString().substring(0, 8);
    promotion =
        promotionRepository.save(Promotion.builder().ref("SEC" + suffix).entryYear(2024).build());
    group =
        groupRepository.save(
            Group.builder().ref("SEK" + suffix).path(Path.EL).promotion(promotion).build());
    groupFlowRepository.save(
        GroupFlow.builder()
            .id(randomUUID().toString())
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(now())
            .build());
    course =
        courseRepository.save(
            Course.builder().code("SECC" + suffix).title("Sécurité").credits(4).build());
    assignment =
        courseAssignmentRepository.save(
            CourseAssignment.builder()
                .id(randomUUID().toString())
                .course(course)
                .teacher(teacher)
                .group(group)
                .year(2024)
                .semester(1)
                .build());
    exam = examRepository.save(aExam(assignment, "Contrôle continu", 0.25));
    otherExam = examRepository.save(aExam(assignment, "Rattrapage", 0.25));
    grade =
        gradeRepository.save(
            Grade.builder()
                .id(randomUUID().toString())
                .examId(exam.getId())
                .studentId(student.getId())
                .score(12.0)
                .isFinal(true)
                .build());
    otherGrade =
        gradeRepository.save(
            Grade.builder()
                .id(randomUUID().toString())
                .examId(otherExam.getId())
                .studentId(otherStudent.getId())
                .score(8.0)
                .isFinal(true)
                .build());
  }

  private Exam aExam(CourseAssignment assignment, String title, double coefficient) {
    return examRepository.save(
        Exam.builder()
            .id(randomUUID().toString())
            .courseAssignmentId(assignment.getId())
            .title(title)
            .examinationDate(now())
            .coefficient(coefficient)
            .build());
  }

  // ---------------------------------------------------------------- users

  @Test
  void admin_lists_users_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            new ParameterizedTypeReference<List<User>>() {});
    assertStatus(HttpStatus.OK, response);
  }

  @Test
  void teacher_cannot_list_users() {
    assertStatus(HttpStatus.FORBIDDEN, getWith(teacherToken, "/users"));
  }

  @Test
  void student_cannot_list_users() {
    assertStatus(HttpStatus.FORBIDDEN, getWith(studentToken, "/users"));
  }

  @Test
  void student_reads_own_profile_ok() {
    assertStatus(HttpStatus.OK, getWith(studentToken, "/users/" + student.getId()));
  }

  @Test
  void student_reads_other_user_profile_forbidden() {
    assertStatus(HttpStatus.FORBIDDEN, getWith(studentToken, "/users/" + otherStudent.getId()));
  }

  @Test
  void student_reads_own_group_flows_ok() {
    assertStatus(
        HttpStatus.OK, getWith(studentToken, "/users/" + student.getId() + "/group_flows"));
  }

  @Test
  void student_reads_other_group_flows_forbidden() {
    assertStatus(
        HttpStatus.FORBIDDEN,
        getWith(studentToken, "/users/" + otherStudent.getId() + "/group_flows"));
  }

  // ---------------------------------------------------------------- promotions

  @Test
  void teacher_reads_promotion_students_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + promotion.getId() + "/students"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(teacherToken)),
            new ParameterizedTypeReference<List<StudentSummaryRest>>() {});
    assertStatus(HttpStatus.OK, response);
    assertEquals(student.getId(), response.getBody().get(0).getId());
  }

  @Test
  void student_cannot_read_promotion_students() {
    assertStatus(
        HttpStatus.FORBIDDEN,
        getWith(studentToken, "/promotions/" + promotion.getId() + "/students"));
  }

  // ---------------------------------------------------------------- grades

  @Test
  void student_reads_own_grade_ok() {
    assertStatus(HttpStatus.OK, getWith(studentToken, "/api/grades/" + grade.getId()));
  }

  @Test
  void student_reads_other_student_grade_forbidden() {
    assertStatus(HttpStatus.FORBIDDEN, getWith(studentToken, "/api/grades/" + otherGrade.getId()));
  }

  @Test
  void teacher_reads_own_course_grade_ok() {
    assertStatus(HttpStatus.OK, getWith(teacherToken, "/api/grades/" + grade.getId()));
  }

  @Test
  void other_teacher_reads_grade_forbidden() {
    assertStatus(HttpStatus.FORBIDDEN, getWith(otherTeacherToken, "/api/grades/" + grade.getId()));
  }

  @Test
  void student_cannot_update_grade() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades/" + grade.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(
                new GradeUpdateRequest(13.0, "Correction", null), authHeaders(studentToken)),
            RestException.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void teacher_updates_own_grade_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/grades/" + grade.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(
                new GradeUpdateRequest(14.5, "Correction collective", true),
                authHeaders(teacherToken)),
            Grade.class);
    assertStatus(HttpStatus.OK, response);
    assertEquals(14.5, response.getBody().getScore());
  }

  // ---------------------------------------------------------------- exams

  @Test
  void teacher_creates_exam_on_own_assignment_ok() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/exams"),
            HttpMethod.POST,
            new HttpEntity<>(
                new ExamCreationRequest(
                    assignment.getId(), "Examen final", now().plusSeconds(3600), 0.5),
                authHeaders(teacherToken)),
            Exam.class);
    assertStatus(HttpStatus.CREATED, response);
    extraExams.add(response.getBody());
  }

  @Test
  void student_cannot_create_exam() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/exams"),
            HttpMethod.POST,
            new HttpEntity<>(
                new ExamCreationRequest(
                    assignment.getId(), "Examen final", now().plusSeconds(3600), 0.5),
                authHeaders(studentToken)),
            RestException.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void student_cannot_delete_exam() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/exams/" + exam.getId()),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(studentToken)),
            RestException.class);
    assertStatus(HttpStatus.FORBIDDEN, response);
  }

  @Test
  void admin_deletes_exam_ok() {
    var gradedExam = aExam(assignment, "Examen final", 0.25);
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/exams/" + gradedExam.getId()),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(adminToken)),
            Void.class);
    assertStatus(HttpStatus.NO_CONTENT, response);
  }

  // ---------------------------------------------------------------- delete conflicts

  @Test
  void delete_user_with_references_is_conflict() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + student.getId()),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(adminToken)),
            RestException.class);
    assertStatus(HttpStatus.CONFLICT, response);
    assertEquals(HttpStatus.CONFLICT.toString(), response.getBody().getType());
  }

  @Test
  void delete_clean_user_ok() {
    var cleanStudent = saveUser(Role.STUDENT, "sec-clean-" + randomUUID() + "@hei.school");
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/users/" + cleanStudent.getId()),
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders(adminToken)),
            Void.class);
    assertStatus(HttpStatus.NO_CONTENT, response);
  }

  // ---------------------------------------------------------------- helpers

  private <T> ResponseEntity<T> getWith(String token, String path, Class<T> responseType) {
    return restTemplate.exchange(
        apiUrl(localPort, path),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders(token)),
        responseType);
  }

  private ResponseEntity<String> getWith(String token, String path) {
    return getWith(token, path, String.class);
  }

  @AfterEach
  void tearDown() {
    gradeHistoryRepository.deleteAll();
    gradeRepository.deleteAll(List.of(grade, otherGrade));
    examRepository.deleteAll(extraExams);
    examRepository.deleteAll(List.of(exam, otherExam));
    courseAssignmentRepository.delete(assignment);
    groupFlowRepository.deleteAll();
    courseRepository.delete(course);
    groupRepository.delete(group);
    promotionRepository.delete(promotion);
    userRepository.deleteAll(List.of(admin, teacher, otherTeacher, student, otherStudent));
  }
}
