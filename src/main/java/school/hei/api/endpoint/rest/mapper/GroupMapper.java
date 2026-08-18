package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.Group;
import school.hei.api.model.dto.GroupRest;

@Component
@AllArgsConstructor
public class GroupMapper {

  public GroupRest toRest(Group group) {
    return GroupRest.builder()
        .id(group.getId())
        .ref(group.getRef())
        .path(group.getPath())
        .promotionId(group.getPromotion().getId())
        .build();
  }

  public List<GroupRest> toRest(List<Group> groups) {
    return groups.stream().map(this::toRest).toList();
  }
}
