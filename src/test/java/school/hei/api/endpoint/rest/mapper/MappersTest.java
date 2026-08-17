package school.hei.api.endpoint.rest.mapper;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.api.model.Course;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;

class MappersTest {

  @Test
  void course_mapper_maps_course_and_list() {
    var mapper = new CourseMapper();
    var course = Course.builder().id("c1").code("CODE1").title("Title").credits(3).build();

    var rest = mapper.toRest(course);
    assertEquals("c1", rest.getId());
    assertEquals("CODE1", rest.getCode());
    assertEquals("Title", rest.getTitle());
    assertEquals(3, rest.getCredits());

    assertEquals(1, mapper.toRest(List.of(course)).size());
  }

  @Test
  void course_assignment_mapper_maps_assignment_and_list() {
    var mapper = new CourseAssignmentMapper();
    var course = Course.builder().id("c1").build();
    var teacher = User.builder().id("t1").build();
    var group = Group.builder().id("g1").build();
    var assignment =
        CourseAssignment.builder()
            .id("a1")
            .course(course)
            .teacher(teacher)
            .group(group)
            .year(2025)
            .semester(2)
            .build();

    var rest = mapper.toRest(assignment);
    assertEquals("a1", rest.getId());
    assertEquals("c1", rest.getCourseId());
    assertEquals("t1", rest.getTeacherId());
    assertEquals("g1", rest.getGroupId());
    assertEquals(2025, rest.getYear());
    assertEquals(2, rest.getSemester());

    assertEquals(1, mapper.toRest(List.of(assignment)).size());
  }

  @Test
  void group_mapper_maps_group_and_list() {
    var mapper = new GroupMapper();
    var promotion = Promotion.builder().id("p1").build();
    var group = Group.builder().id("g1").ref("G1").path(Path.EL).promotion(promotion).build();

    var rest = mapper.toRest(group);
    assertEquals("g1", rest.getId());
    assertEquals("G1", rest.getRef());
    assertEquals(Path.EL, rest.getPath());
    assertEquals("p1", rest.getPromotionId());

    assertEquals(1, mapper.toRest(List.of(group)).size());
  }

  @Test
  void group_flow_mapper_maps_flow_and_list() {
    var mapper = new GroupFlowMapper();
    var group = Group.builder().id("g1").build();
    var student = User.builder().id("s1").build();
    var flowDatetime = now();
    var flow =
        GroupFlow.builder()
            .id("f1")
            .group(group)
            .student(student)
            .flowType(FlowType.JOIN)
            .flowDatetime(flowDatetime)
            .build();

    var rest = mapper.toRest(flow);
    assertEquals("f1", rest.getId());
    assertEquals("g1", rest.getGroupId());
    assertEquals("s1", rest.getStudentId());
    assertEquals(FlowType.JOIN, rest.getFlowType());
    assertEquals(flowDatetime, rest.getFlowDatetime());

    assertEquals(1, mapper.toRest(List.of(flow)).size());
  }

  @Test
  void promotion_mapper_maps_promotion_and_list() {
    var mapper = new PromotionMapper();
    var promotion = Promotion.builder().id("p1").ref("P1").entryYear(2025).build();

    var rest = mapper.toRest(promotion);
    assertEquals("p1", rest.getId());
    assertEquals("P1", rest.getRef());
    assertEquals(2025, rest.getEntryYear());

    assertEquals(1, mapper.toRest(List.of(promotion)).size());
  }

  @Test
  void user_mapper_maps_user_and_list() {
    var mapper = new UserMapper();
    var createdAt = now();
    var user =
        User.builder()
            .id("u1")
            .firstName("First")
            .lastName("Last")
            .email("user@hei.school")
            .role(Role.ADMIN)
            .createdAt(createdAt)
            .build();

    var rest = mapper.toRest(user);
    assertEquals("u1", rest.getId());
    assertEquals("First", rest.getFirstName());
    assertEquals("Last", rest.getLastName());
    assertEquals("user@hei.school", rest.getEmail());
    assertEquals(Role.ADMIN, rest.getRole());
    assertEquals(createdAt, rest.getCreatedAt());

    assertEquals(1, mapper.toRest(List.of(user)).size());
  }
}
