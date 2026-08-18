package school.hei.api.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.api.model.GroupFlow;
import school.hei.api.model.dto.GroupFlowRest;

@Component
@AllArgsConstructor
public class GroupFlowMapper {

  public GroupFlowRest toRest(GroupFlow flow) {
    return GroupFlowRest.builder()
        .id(flow.getId())
        .groupId(flow.getGroup().getId())
        .studentId(flow.getStudent().getId())
        .flowType(flow.getFlowType())
        .flowDatetime(flow.getFlowDatetime())
        .build();
  }

  public List<GroupFlowRest> toRest(List<GroupFlow> flows) {
    return flows.stream().map(this::toRest).toList();
  }
}
