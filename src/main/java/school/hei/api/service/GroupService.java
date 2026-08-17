package school.hei.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.CourseAssignment;
import school.hei.api.model.Group;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.GroupCreation;
import school.hei.api.model.dto.GroupFlowCreation;
import school.hei.api.model.dto.GroupFlowRest;
import school.hei.api.model.dto.GroupRest;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ApiException;
import school.hei.api.model.exception.ApiExceptionType;
import school.hei.api.repository.CourseAssignmentRepository;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.GroupRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupFlowRepository groupFlowRepository;
  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

  public List<GroupRest> getAll(String promotionId, Path path) {
    List<Group> groups;
    if (promotionId != null && path != null) {
      groups = groupRepository.findByPromotionIdAndPath(promotionId, path);
    } else if (promotionId != null) {
      groups = groupRepository.findByPromotionId(promotionId);
    } else if (path != null) {
      groups = groupRepository.findByPath(path);
    } else {
      groups = groupRepository.findAll();
    }
    return groups.stream().map(this::toRest).toList();
  }

  public GroupRest getById(String id) {
    return toRest(getEntityById(id));
  }

  @Transactional
  public GroupRest create(GroupCreation creation) {
    Promotion promotion = getPromotionOrThrow(creation.getPromotionId());
    Group group =
        Group.builder()
            .ref(creation.getRef())
            .path(creation.getPath())
            .promotion(promotion)
            .build();
    return toRest(groupRepository.save(group));
  }

  @Transactional
  public GroupRest update(String id, GroupCreation creation) {
    Group group = getEntityById(id);
    Promotion promotion = getPromotionOrThrow(creation.getPromotionId());
    group.setRef(creation.getRef());
    group.setPath(creation.getPath());
    group.setPromotion(promotion);
    return toRest(groupRepository.save(group));
  }

  @Transactional
  public void delete(String id) {
    if (!groupRepository.existsById(id)) {
      throw new ApiException(ApiExceptionType.NOT_FOUND, "Group " + id + " not found");
    }
    groupRepository.deleteById(id);
  }

  @Transactional
  public GroupFlowRest recordFlow(String groupId, GroupFlowCreation creation) {
    Group group = getEntityById(groupId);
    User student = getStudentOrThrow(creation.getStudentId());

    Optional<GroupFlow> lastFlow =
        groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc(student.getId());
    boolean currentlyActive = lastFlow.isPresent() && lastFlow.get().getFlowType() == FlowType.JOIN;

    if (creation.getFlowType() == FlowType.JOIN && currentlyActive) {
      throw new ApiException(
          ApiExceptionType.BAD_REQUEST,
          "Student " + student.getId() + " is already active in a group; LEAVE it first");
    }
    if (creation.getFlowType() == FlowType.LEAVE && !currentlyActive) {
      throw new ApiException(
          ApiExceptionType.BAD_REQUEST,
          "Student " + student.getId() + " is not currently active in any group");
    }

    GroupFlow flow =
        GroupFlow.builder()
            .group(group)
            .student(student)
            .flowType(creation.getFlowType())
            .flowDatetime(
                creation.getFlowDatetime() != null ? creation.getFlowDatetime() : Instant.now())
            .build();
    return toRest(groupFlowRepository.save(flow));
  }

  public List<GroupFlowRest> getGroupFlowHistory(String studentId) {
    getStudentOrThrow(studentId);
    return groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId).stream()
        .map(this::toRest)
        .toList();
  }

  public Optional<GroupRest> getCurrentGroup(String studentId) {
    getStudentOrThrow(studentId);
    return groupFlowRepository
        .findFirstByStudentIdOrderByFlowDatetimeDesc(studentId)
        .filter(flow -> flow.getFlowType() == FlowType.JOIN)
        .map(flow -> toRest(flow.getGroup()));
  }

  public List<CourseAssignment> getCourseAssignmentsFollowedByStudent(String studentId) {
    getStudentOrThrow(studentId);
    List<GroupFlow> history = groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId);

    List<CourseAssignment> result = new ArrayList<>();
    Group activeGroup = null;
    Instant activeSince = null;

    for (GroupFlow event : history) {
      if (event.getFlowType() == FlowType.JOIN) {
        activeGroup = event.getGroup();
        activeSince = event.getFlowDatetime();
      } else if (event.getFlowType() == FlowType.LEAVE && activeGroup != null) {
        result.addAll(courseAssignmentsInPeriod(activeGroup, activeSince, event.getFlowDatetime()));
        activeGroup = null;
        activeSince = null;
      }
    }
    if (activeGroup != null) {
      result.addAll(courseAssignmentsInPeriod(activeGroup, activeSince, Instant.now()));
    }
    return result;
  }

  private List<CourseAssignment> courseAssignmentsInPeriod(Group group, Instant from, Instant to) {
    int fromYear = from.atZone(java.time.ZoneOffset.UTC).getYear();
    int toYear = to.atZone(java.time.ZoneOffset.UTC).getYear();
    return courseAssignmentRepository.findByGroupId(group.getId()).stream()
        .filter(assignment -> assignment.getYear() >= fromYear && assignment.getYear() <= toYear)
        .toList();
  }

  private Group getEntityById(String id) {
    return groupRepository
        .findById(id)
        .orElseThrow(
            () -> new ApiException(ApiExceptionType.NOT_FOUND, "Group " + id + " not found"));
  }

  private Promotion getPromotionOrThrow(String promotionId) {
    return promotionRepository
        .findById(promotionId)
        .orElseThrow(
            () ->
                new ApiException(
                    ApiExceptionType.NOT_FOUND, "Promotion " + promotionId + " not found"));
  }

  private User getStudentOrThrow(String studentId) {
    User user =
        userRepository
            .findById(studentId)
            .orElseThrow(
                () ->
                    new ApiException(
                        ApiExceptionType.NOT_FOUND, "User " + studentId + " not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ApiException(
          ApiExceptionType.BAD_REQUEST, "User " + studentId + " is not a student");
    }
    return user;
  }

  private GroupRest toRest(Group group) {
    return GroupRest.builder()
        .id(group.getId())
        .ref(group.getRef())
        .path(group.getPath())
        .promotionId(group.getPromotion().getId())
        .build();
  }

  private GroupFlowRest toRest(GroupFlow flow) {
    return GroupFlowRest.builder()
        .id(flow.getId())
        .groupId(flow.getGroup().getId())
        .studentId(flow.getStudent().getId())
        .flowType(flow.getFlowType())
        .flowDatetime(flow.getFlowDatetime())
        .build();
  }
}
