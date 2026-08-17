package school.hei.api.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Path;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.BadRequestException;
import school.hei.api.model.exception.NotFoundException;
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

  public List<Group> getAll(String promotionId, Path path) {
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
    return groups;
  }

  public Group getById(String id) {
    return getEntityById(id);
  }

  @Transactional
  public Group create(GroupCreation creation) {
    Promotion promotion = getPromotionOrThrow(creation.getPromotionId());
    Group group =
        Group.builder()
            .ref(creation.getRef())
            .path(creation.getPath())
            .promotion(promotion)
            .build();
    return groupRepository.save(group);
  }

  @Transactional
  public Group update(String id, GroupCreation creation) {
    Group group = getEntityById(id);
    Promotion promotion = getPromotionOrThrow(creation.getPromotionId());
    group.setRef(creation.getRef());
    group.setPath(creation.getPath());
    group.setPromotion(promotion);
    return groupRepository.save(group);
  }

  @Transactional
  public void delete(String id) {
    if (!groupRepository.existsById(id)) {
      throw new NotFoundException("Group " + id + " not found");
    }
    groupRepository.deleteById(id);
  }

  @Transactional
  public GroupFlow recordFlow(String groupId, GroupFlowCreation creation) {
    Group group = getEntityById(groupId);
    User student = getStudentOrThrow(creation.getStudentId());

    Optional<GroupFlow> lastFlow =
        groupFlowRepository.findFirstByStudentIdOrderByFlowDatetimeDesc(student.getId());
    boolean currentlyActive = lastFlow.isPresent() && lastFlow.get().getFlowType() == FlowType.JOIN;

    if (creation.getFlowType() == FlowType.JOIN && currentlyActive) {
      throw new BadRequestException(
          "Student " + student.getId() + " is already active in a group; LEAVE it first");
    }
    if (creation.getFlowType() == FlowType.LEAVE && !currentlyActive) {
      throw new BadRequestException(
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
    return groupFlowRepository.save(flow);
  }

  public List<GroupFlow> getGroupFlowHistory(String studentId) {
    getStudentOrThrow(studentId);
    return groupFlowRepository.findByStudentIdOrderByFlowDatetimeAsc(studentId);
  }

  public Optional<Group> getCurrentGroup(String studentId) {
    getStudentOrThrow(studentId);
    return groupFlowRepository
        .findFirstByStudentIdOrderByFlowDatetimeDesc(studentId)
        .filter(flow -> flow.getFlowType() == FlowType.JOIN)
        .map(GroupFlow::getGroup);
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
    return courseAssignmentRepository.findByGroupId(group.getId()).stream()
        .filter(assignment -> overlapsMembershipPeriod(assignment, from, to))
        .toList();
  }

  private boolean overlapsMembershipPeriod(CourseAssignment assignment, Instant from, Instant to) {
    LocalDate periodStart = semesterStart(assignment.getYear(), assignment.getSemester());
    LocalDate periodEnd = semesterEnd(assignment.getYear(), assignment.getSemester());
    LocalDate membershipFrom = from.atZone(ZoneOffset.UTC).toLocalDate();
    LocalDate membershipTo = to.atZone(ZoneOffset.UTC).toLocalDate();
    return !periodStart.isAfter(membershipTo) && !periodEnd.isBefore(membershipFrom);
  }

  private LocalDate semesterStart(int year, int semester) {
    return semester == 1 ? LocalDate.of(year, 1, 1) : LocalDate.of(year, 7, 1);
  }

  private LocalDate semesterEnd(int year, int semester) {
    return semester == 1 ? LocalDate.of(year, 6, 30) : LocalDate.of(year, 12, 31);
  }

  private Group getEntityById(String id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Group " + id + " not found"));
  }

  private Promotion getPromotionOrThrow(String promotionId) {
    return promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new NotFoundException("Promotion " + promotionId + " not found"));
  }

  private User getStudentOrThrow(String studentId) {
    User user =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("User " + studentId + " not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new BadRequestException("User " + studentId + " is not a student");
    }
    return user;
  }
}
