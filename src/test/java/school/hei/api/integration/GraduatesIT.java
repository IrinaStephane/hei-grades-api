package school.hei.api.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.api.integration.conf.ApiAssertions.assertStatus;
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
import school.hei.api.service.GraduateService.Graduate;

class GraduatesIT extends FacadeITMockedThirdParties {

  @Autowired private PromotionRepository promotionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  private User admin;
  private User teacher;
  private User student1;
  private User student2;
  private Promotion promotion;
  private Group group;

  @BeforeEach
  void setUp() {
    admin = saveUser(Role.ADMIN, "grad-admin-" + randomUUID() + "@hei.school");
    teacher = saveUser(Role.TEACHER, "grad-teacher-" + randomUUID() + "@hei.school");
    student1 = saveUser(Role.STUDENT, "grad-s1-" + randomUUID() + "@hei.school");
    student2 = saveUser(Role.STUDENT, "grad-s2-" + randomUUID() + "@hei.school");

    promotion =
        promotionRepository.save(
            Promotion.builder().ref("PROMO-" + randomUUID()).entryYear(2024).build());
    group =
        groupRepository.save(
            Group.builder().ref("K1-" + randomUUID()).path(Path.EL).promotion(promotion).build());
  }

  @Test
  void graduates_json_sorted_by_rank() {
    var course =
        courseRepository.save(
            Course.builder().code("CODE-" + randomUUID()).title("Course").credits(30).build());
    var ca =
        courseAssignmentRepository.save(
            CourseAssignment.builder()
                .course(course)
                .teacher(teacher)
                .group(group)
                .year(2024)
                .semester(1)
                .build());
    var exam =
        examRepository.save(
            school.hei.api.repository.model.Exam.builder()
                .id(randomUUID().toString())
                .courseAssignmentId(ca.getId())
                .title("Exam")
                .examinationDate(Instant.now())
                .coefficient(1.0)
                .build());
    gradeRepository.save(
        school.hei.api.repository.model.Grade.builder()
            .id(randomUUID().toString())
            .examId(exam.getId())
            .studentId(student1.getId())
            .score(18.0)
            .isFinal(true)
            .build());
    gradeRepository.save(
        school.hei.api.repository.model.Grade.builder()
            .id(randomUUID().toString())
            .examId(exam.getId())
            .studentId(student2.getId())
            .score(12.0)
            .isFinal(true)
            .build());

    groupFlowRepository.save(
        GroupFlow.builder()
            .group(group)
            .student(student1)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build());
    groupFlowRepository.save(
        GroupFlow.builder()
            .group(group)
            .student(student2)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build());

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/promotions/" + promotion.getId() + "/graduates?path=EL"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            new ParameterizedTypeReference<List<Graduate>>() {});

    assertStatus(HttpStatus.OK, response);
    assertEquals(2, response.getBody().size());
    assertEquals(student1.getId(), response.getBody().get(0).getStudentId());
    assertEquals(student2.getId(), response.getBody().get(1).getStudentId());
    assertTrue(
        response.getBody().get(0).getGeneralAverage()
            > response.getBody().get(1).getGeneralAverage());
  }

  @Test
  void graduates_export_returns_302() {
    groupFlowRepository.save(
        GroupFlow.builder()
            .group(group)
            .student(student1)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build());

    var response =
        restTemplate.exchange(
            apiUrl(localPort, "/api/promotions/" + promotion.getId() + "/graduates/export?path=EL"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(admin)),
            Object.class);

    assertStatus(HttpStatus.FOUND, response);
    assertNotNull(response.getHeaders().getLocation());
  }

  @AfterEach
  void tearDown() {
    gradeRepository.findByStudentId(student1.getId()).forEach(gradeRepository::delete);
    gradeRepository.findByStudentId(student2.getId()).forEach(gradeRepository::delete);
    courseAssignmentRepository
        .findByGroupId(group.getId())
        .forEach(
            ca -> {
              examRepository.findByCourseAssignmentId(ca.getId()).forEach(examRepository::delete);
              courseAssignmentRepository.delete(ca);
            });
    groupFlowRepository
        .findByGroupIdOrderByFlowDatetimeAsc(group.getId())
        .forEach(groupFlowRepository::delete);
    groupRepository.delete(group);
    promotionRepository.delete(promotion);
    userRepository.delete(student1);
    userRepository.delete(student2);
    userRepository.delete(teacher);
    userRepository.delete(admin);
  }
}
