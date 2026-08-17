package school.hei.api.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.Promotion;
import school.hei.api.model.User;
import school.hei.api.model.dto.GroupRest;
import school.hei.api.model.dto.PromotionCreation;
import school.hei.api.model.dto.StudentSummaryRest;
import school.hei.api.model.enums.FlowType;
import school.hei.api.model.enums.Role;
import school.hei.api.model.exception.ConflictException;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.GroupFlowRepository;
import school.hei.api.repository.PromotionRepository;
import school.hei.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;
  private final GroupFlowRepository groupFlowRepository;

  public List<Promotion> getAll() {
    return promotionRepository.findAll();
  }

  public Promotion getById(String id) {
    return getEntityById(id);
  }

  @Transactional
  public Promotion create(PromotionCreation creation) {
    if (promotionRepository.existsByRef(creation.getRef())) {
      throw new ConflictException("Promotion " + creation.getRef() + " already exists");
    }
    Promotion promotion =
        Promotion.builder().ref(creation.getRef()).entryYear(creation.getEntryYear()).build();
    return promotionRepository.save(promotion);
  }

  @Transactional
  public Promotion update(String id, PromotionCreation creation) {
    Promotion promotion = getEntityById(id);
    promotion.setRef(creation.getRef());
    promotion.setEntryYear(creation.getEntryYear());
    return promotionRepository.save(promotion);
  }

  @Transactional
  public void delete(String id) {
    if (!promotionRepository.existsById(id)) {
      throw new NotFoundException("Promotion " + id + " not found");
    }
    promotionRepository.deleteById(id);
  }

  public List<StudentSummaryRest> getStudents(String promotionId) {
    getEntityById(promotionId);
    return userRepository.findByRole(Role.STUDENT).stream()
        .map(toStudentSummaryIfInPromotion(promotionId))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private java.util.function.Function<User, StudentSummaryRest> toStudentSummaryIfInPromotion(
      String promotionId) {
    return student -> {
      GroupFlow lastFlow =
          groupFlowRepository
              .findFirstByStudentIdOrderByFlowDatetimeDesc(student.getId())
              .orElse(null);
      if (lastFlow == null || lastFlow.getFlowType() != FlowType.JOIN) {
        return null;
      }
      if (!lastFlow.getGroup().getPromotion().getId().equals(promotionId)) {
        return null;
      }
      return StudentSummaryRest.builder()
          .id(student.getId())
          .firstName(student.getFirstName())
          .lastName(student.getLastName())
          .currentGroup(
              GroupRest.builder()
                  .id(lastFlow.getGroup().getId())
                  .ref(lastFlow.getGroup().getRef())
                  .path(lastFlow.getGroup().getPath())
                  .promotionId(promotionId)
                  .build())
          .build();
    };
  }

  private Promotion getEntityById(String id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Promotion " + id + " not found"));
  }
}