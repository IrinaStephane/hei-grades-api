package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.Promotion;
import school.hei.api.model.dto.PromotionRest;

@Component
@AllArgsConstructor
public class PromotionMapper {

  public PromotionRest toRest(Promotion promotion) {
    return PromotionRest.builder()
        .id(promotion.getId())
        .ref(promotion.getRef())
        .entryYear(promotion.getEntryYear())
        .build();
  }

  public List<PromotionRest> toRest(List<Promotion> promotions) {
    return promotions.stream().map(this::toRest).toList();
  }
}
