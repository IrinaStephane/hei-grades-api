package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.model.dto.GroupCreation;
import school.hei.api.model.dto.GroupFlowCreation;
import school.hei.api.model.dto.GroupFlowRest;
import school.hei.api.model.dto.GroupRest;
import school.hei.api.model.enums.Path;
import school.hei.api.service.GroupService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public List<GroupRest> getGroups(
      @RequestParam(required = false) String promotionId, @RequestParam(required = false) Path path) {
    return groupService.getAll(promotionId, path);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public GroupRest createGroup(@Valid @RequestBody GroupCreation creation) {
    return groupService.create(creation);
  }

  @GetMapping("/{id}")
  public GroupRest getGroupById(@PathVariable String id) {
    return groupService.getById(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public GroupRest updateGroup(@PathVariable String id, @Valid @RequestBody GroupCreation creation) {
    return groupService.update(id, creation);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteGroup(@PathVariable String id) {
    groupService.delete(id);
  }

  @PostMapping("/{id}/flows")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public GroupFlowRest createGroupFlow(
      @PathVariable String id, @Valid @RequestBody GroupFlowCreation creation) {
    return groupService.recordFlow(id, creation);
  }
}