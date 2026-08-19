package school.hei.api.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.api.endpoint.rest.security.model.Principal;
import school.hei.api.repository.GradeRepository;

public class GradeSelfMatcher extends RequestIdMatcher {
  private final GradeRepository gradeRepository;

  public GradeSelfMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      GradeRepository gradeRepository) {
    super(method, antPattern, stringBeforeId);
    this.gradeRepository = gradeRepository;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Principal principal)) {
      return false;
    }
    return gradeRepository
        .findById(getRequestId(request))
        .map(grade -> Objects.equals(grade.getStudentId(), principal.getUserId()))
        .orElse(false);
  }
}
