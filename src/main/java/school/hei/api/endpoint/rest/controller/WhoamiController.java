package school.hei.api.endpoint.rest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.model.Whoami;
import school.hei.api.endpoint.rest.security.model.Principal;

@RestController
public class WhoamiController {

  @GetMapping("/whoami")
  public Whoami whoami(@AuthenticationPrincipal Principal principal) {
    return new Whoami(principal.getUserId(), principal.getRole());
  }
}
