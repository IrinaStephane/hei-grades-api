package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
import static school.hei.api.integration.conf.TestUtils.apiUrl;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import school.hei.api.file.hash.FileHash;
import school.hei.api.file.hash.FileHashAlgorithm;
import school.hei.api.integration.conf.FacadeITMockedThirdParties;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.CourseRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;
import school.hei.api.service.GraduateService.Graduate;

class GraduatesIT extends FacadeITMockedThirdParties {

  private static final int ENTRY_YEAR = 2024;

  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  private User admin;
  private User teacher;
  private User s1;
  private User s2;
  private User s3;
  private User s4;
  private Promotion promotion;
  private Group k1;
  private Group k2;
  private Group k3;
  private Course c2024;
  private Course c2025;
  private Course c2026;
  private CourseAssignment ca24;
  private CourseAssignment ca25;
  private CourseAssignment ca26;
  private CourseAssignment ca24tn;
  private CourseAssignment ca25tn;
  private CourseAssignment ca26tn;
  private Exam e24;
  private Exam e25;
  private Exam e26;
  private Exam e24tn;
  private Exam e25tn;
  private Exam e26tn;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "grad-admin-" + randomUUID() + "@hei.school");
    teacher = saveUser(Role.TEACHER, "grad-teacher-" + randomUUID() + "@hei.school");
    s1 = saveUser(Role.STUDENT, "grad-s1-" + randomUUID() + "@hei.school");
    s2 = saveUser(Role.STUDENT, "grad-s2-" + randomUUID() + "@hei.school");
    s3 = saveUser(Role.STUDENT, "grad-s3-" + randomUUID() + "@hei.school");
    s4 = saveUser(Role.STUDENT, "grad-s4-" + randomUUID() + "@hei.school");

    when(bucketComponent.upload(any(), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.NONE, null));
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(URI.create("https://s3.example.com/presigned").toURL());

    promotion =
        promotionRepository.save(
            Promotion.builder().ref("PROMO-" + randomUUID()).entryYear(ENTRY_YEAR).build());
    k1 =
        groupRepository.save(
            Group.builder().ref("K1-" + randomUUID()).path(Path.EL).promotion(promotion).build());
    k2 =
        groupRepository.save(
            Group.builder().ref("K2-" + randomUUID()).path(Path.EL).promotion(promotion).build());
    k3 =
        groupRepository.save(
            Group.builder().ref("K3-" + randomUUID()).path(Path.TN).promotion(promotion).build());

    c2024 = saveCourse("C2024");
    c2025 = saveCourse("C2025");
    c2026 = saveCourse("C2026");

    ca24 = saveAssignment(c2024, k1, ENTRY_YEAR);
    ca25 = saveAssignment(c2025, k2, ENTRY_YEAR + 1);
    ca26 = saveAssignment(c2026, k1, ENTRY_YEAR + 2);
    ca24tn = saveAssignment(c2024, k3, ENTRY_YEAR);
    ca25tn = saveAssignment(c2025, k3, ENTRY_YEAR + 1);
    ca26tn = saveAssignment(c2026, k3, ENTRY_YEAR + 2);

    e24 = saveExam(ca24);
    e25 = saveExam(ca25);
    e26 = saveExam(ca26);
    e24tn = saveExam(ca24tn);
    e25tn = saveExam(ca25tn);
    e26tn = saveExam(ca26tn);

    saveFlow(k1, s1, FlowType.JOIN, ENTRY_YEAR);
    saveFlow(k1, s1, FlowType.LEAVE, ENTRY_YEAR + 1);
    saveFlow(k2, s1, FlowType.JOIN, ENTRY_YEAR + 1);
    saveFlow(k2, s1, FlowType.LEAVE, ENTRY_YEAR + 2);
    saveFlow(k1, s1, FlowType.JOIN, ENTRY_YEAR + 2);
    saveFlow(k1, s2, FlowType.JOIN, ENTRY_YEAR);
    saveFlow(k1, s3, FlowType.JOIN, ENTRY_YEAR);
    saveFlow(k1, s3, FlowType.JOIN, ENTRY_YEAR + 1);
    saveFlow(k1, s3, FlowType.JOIN, ENTRY_YEAR + 2);
    saveFlow(k3, s4, FlowType.JOIN, ENTRY_YEAR);
    saveFlow(k3, s4, FlowType.JOIN, ENTRY_YEAR + 1);
    saveFlow(k3, s4, FlowType.JOIN, ENTRY_YEAR + 2);
  }

  private Course saveCourse(String code) {
    return courseRepository.save(
        Course.builder()
            .code(code + "-" + randomUUID())
            .title("Course " + code)
            .credits(30)
            .build());
  }

  private CourseAssignment saveAssignment(Course course, Group group, int year) {
    return courseAssignmentRepository.save(
        CourseAssignment.builder()
            .course(course)
            .teacher(teacher)
            .group(group)
            .year(year)
            .semester(1)
            .build());
  }

  private Exam saveExam(CourseAssignment assignment) {
    return examRepository.save(
        Exam.builder()
            .id(randomUUID().toString())
            .courseAssignmentId(assignment.getId())
            .title("Examen final")
            .examinationDate(Instant.now())
            .coefficient(1.0)
            .build());
  }

  private void saveFlow(Group group, User student, FlowType flowType, int year) {
    groupFlowRepository.save(
        GroupFlow.builder()
            .id(randomUUID().toString())
            .group(group)
            .student(student)
            .flowType(flowType)
            .flowDatetime(Instant.parse(year + "-09-01T08:00:00Z"))
            .build());
  }

  private void saveGrade(User student, Exam exam, double score) {
    gradeRepository.save(
        Grade.builder()
            .id(randomUUID().toString())
            .examId(exam.getId())
            .studentId(student.getId())
            .score(score)
            .isFinal(true)
            .build());
  }

  private List<Graduate> getGraduates(String path) {
    var url = "/promotions/" + promotion.getId() + "/graduates";
    var response =
        restTemplate.exchange(
            apiUrl(localPort, path == null ? url : url + "?path=" + path),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            new ParameterizedTypeReference<List<Graduate>>() {});
    assertStatus(HttpStatus.OK, response);
    return response.getBody();
  }

  @Test
  void graduates_include_only_students_who_completed_the_full_curriculum() {
    // s1 switched groups mid-curriculum and passed everything: graduate.
    saveGrade(s1, e24, 15.0);
    saveGrade(s1, e25, 14.0);
    saveGrade(s1, e26, 16.0);
    // s2 attended only the first year: not a graduate.
    saveGrade(s2, e24, 15.0);
    // s3 attended three years but failed the last course: not a graduate.
    saveGrade(s3, e24, 15.0);
    saveGrade(s3, e25, 14.0);
    saveGrade(s3, e26, 8.0);
    // s4 followed the TN track and passed everything: graduate.
    saveGrade(s4, e24tn, 12.0);
    saveGrade(s4, e25tn, 13.0);
    saveGrade(s4, e26tn, 11.0);

    var elGraduates = getGraduates("EL");
    var tnGraduates = getGraduates("TN");
    var allGraduates = getGraduates(null);

    assertEquals(1, elGraduates.size());
    assertEquals(s1.getId(), elGraduates.get(0).getStudentId());
    assertEquals(1, tnGraduates.size());
    assertEquals(s4.getId(), tnGraduates.get(0).getStudentId());
    assertEquals(2, allGraduates.size());
    assertEquals(s1.getId(), allGraduates.get(0).getStudentId());
    assertEquals(s4.getId(), allGraduates.get(1).getStudentId());
    assertTrue(allGraduates.get(0).getGeneralAverage() > allGraduates.get(1).getGeneralAverage());
  }

  @Test
  void graduates_export_returns_302() {
    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/promotions/" + promotion.getId() + "/graduates/export"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            Object.class);

    assertStatus(HttpStatus.FOUND, response);
    assertNotNull(response.getHeaders().getLocation());
  }

  @AfterEach
  void tearDown() {
    groupFlowRepository
        .findByGroupIdOrderByFlowDatetimeAsc(k1.getId())
        .forEach(groupFlowRepository::delete);
    groupFlowRepository
        .findByGroupIdOrderByFlowDatetimeAsc(k2.getId())
        .forEach(groupFlowRepository::delete);
    groupFlowRepository
        .findByGroupIdOrderByFlowDatetimeAsc(k3.getId())
        .forEach(groupFlowRepository::delete);
    gradeRepository.findByStudentId(s1.getId()).forEach(gradeRepository::delete);
    gradeRepository.findByStudentId(s2.getId()).forEach(gradeRepository::delete);
    gradeRepository.findByStudentId(s3.getId()).forEach(gradeRepository::delete);
    gradeRepository.findByStudentId(s4.getId()).forEach(gradeRepository::delete);
    List.of(k1, k2, k3)
        .forEach(
            group ->
                courseAssignmentRepository
                    .findByGroupId(group.getId())
                    .forEach(
                        ca -> {
                          examRepository
                              .findByCourseAssignmentId(ca.getId())
                              .forEach(examRepository::delete);
                          courseAssignmentRepository.delete(ca);
                        }));
    groupRepository.delete(k1);
    groupRepository.delete(k2);
    groupRepository.delete(k3);
    promotionRepository.delete(promotion);
    courseRepository.delete(c2024);
    courseRepository.delete(c2025);
    courseRepository.delete(c2026);
    userRepository.delete(s1);
    userRepository.delete(s2);
    userRepository.delete(s3);
    userRepository.delete(s4);
    userRepository.delete(teacher);
    userRepository.delete(admin);
  }
}
