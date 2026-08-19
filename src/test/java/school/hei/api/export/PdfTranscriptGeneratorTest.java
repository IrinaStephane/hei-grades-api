package school.hei.api.export;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.ExamRepository;
import school.hei.api.repository.GradeRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.model.Exam;
import school.hei.api.repository.model.Grade;

class PdfTranscriptGeneratorTest {

  private final GroupFlowRepository groupFlowRepository = mock(GroupFlowRepository.class);
  private final CourseAssignmentRepository courseAssignmentRepository =
      mock(CourseAssignmentRepository.class);
  private final ExamRepository examRepository = mock(ExamRepository.class);
  private final GradeRepository gradeRepository = mock(GradeRepository.class);
  private final PdfTranscriptGenerator subject =
      new PdfTranscriptGenerator(
          groupFlowRepository, courseAssignmentRepository, examRepository, gradeRepository);

  private static User aStudent() {
    return User.builder()
        .id("s1")
        .firstName("John")
        .lastName("Doe")
        .email("john@hei.school")
        .passwordHash("hash")
        .role(Role.STUDENT)
        .createdAt(Instant.now())
        .build();
  }

  @Test
  void generate_with_year_produces_pdf_file() {
    var student = aStudent();
    var group = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build();
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(List.of(flow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());

    var file = subject.generate(student, 2025);

    assertTrue(file.exists());
    assertTrue(file.getName().startsWith("transcript-s1"));
    assertTrue(file.getName().endsWith(".pdf"));
    assertTrue(file.length() > 0);
  }

  @Test
  void generate_without_year_produces_pdf_file() {
    var student = aStudent();
    var group = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build();
    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(List.of(flow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());

    var file = subject.generate(student, null);

    assertTrue(file.exists());
    assertTrue(file.length() > 0);
  }

  @Test
  void generate_with_courses_shows_transcript_data() {
    var student = aStudent();
    var group = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build();
    var assignment =
        CourseAssignment.builder()
            .id("ca1")
            .group(group)
            .course(
                school.hei.api.model.Course.builder()
                    .id("c1")
                    .code("INF101")
                    .title("Math")
                    .credits(30)
                    .build())
            .year(2025)
            .semester(1)
            .build();
    var exam =
        Exam.builder().id("e1").courseAssignmentId("ca1").coefficient(1.0).title("Final").build();
    var grade =
        Grade.builder().id("g1").examId("e1").studentId("s1").score(15.0).isFinal(true).build();

    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(List.of(flow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of(assignment));
    when(examRepository.findByCourseAssignmentId("ca1")).thenReturn(List.of(exam));
    when(gradeRepository.findByExamId("e1")).thenReturn(List.of(grade));
    when(gradeRepository.findByStudentId("s1")).thenReturn(List.of(grade));

    var file = subject.generate(student, 2025);

    assertTrue(file.exists());
    assertTrue(file.length() > 0);
  }

  @Test
  void compute_status_provisoire_when_non_final_grades() {
    var student = aStudent();
    var group = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build();
    var grade =
        Grade.builder().id("g1").examId("e1").studentId("s1").score(15.0).isFinal(false).build();

    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(List.of(flow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());
    when(gradeRepository.findByStudentId("s1")).thenReturn(List.of(grade));

    var file = subject.generate(student, 2025);

    assertTrue(file.exists());
  }

  @Test
  void compute_status_complet_when_all_final_grades() {
    var student = aStudent();
    var group = Group.builder().id("g1").ref("K1").path(Path.EL).build();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(Instant.now())
            .build();
    var grade =
        Grade.builder().id("g1").examId("e1").studentId("s1").score(15.0).isFinal(true).build();

    when(groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc("s1")).thenReturn(List.of(flow));
    when(courseAssignmentRepository.findByGroupId("g1")).thenReturn(List.of());
    when(gradeRepository.findByStudentId("s1")).thenReturn(List.of(grade));

    var file = subject.generate(student, 2025);

    assertTrue(file.exists());
  }
}
