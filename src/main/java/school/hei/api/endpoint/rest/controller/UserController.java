package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import school.hei.api.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.api.endpoint.rest.mapper.UserMapper;
import school.hei.api.model.dto.GroupFlowRest;
import school.hei.api.model.dto.UserCreation;
import school.hei.api.model.dto.UserRest;
import school.hei.api.model.dto.UserUpdate;
import school.hei.api.model.enums.Role;
import school.hei.api.service.GroupService;
import school.hei.api.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final GroupService groupService;
  private final GroupFlowMapper groupFlowMapper;

  @GetMapping
  public List<UserRest> getUsers(@RequestParam(required = false) Role role) {
    return userMapper.toRest(userService.getAll(role));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserRest createUser(@Valid @RequestBody UserCreation creation) {
    return userMapper.toRest(userService.create(creation));
  }

  @GetMapping("/{id}")
  public UserRest getUserById(@PathVariable String id) {
    return userMapper.toRest(userService.getById(id));
  }

  @PutMapping("/{id}")
  public UserRest updateUser(@PathVariable String id, @Valid @RequestBody UserUpdate update) {
    return userMapper.toRest(userService.update(id, update));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable String id) {
    userService.delete(id);
  }

  @GetMapping("/{id}/group_flows")
  public List<GroupFlowRest> getUserGroupFlows(@PathVariable String id) {
    return groupFlowMapper.toRest(groupService.getGroupFlowHistory(id));
  }

  // POST /{id}/transcript is NOT implemented here: it triggers the async
  // PDF generation + email flow, which is Personne B's responsibility
  // (mail + endpoint/event package). Coordinate with them on where the
  // handler for this route lives.
}
