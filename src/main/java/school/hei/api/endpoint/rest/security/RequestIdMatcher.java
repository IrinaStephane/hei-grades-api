package school.hei.api.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;

@AllArgsConstructor
public abstract class RequestIdMatcher implements RequestMatcher {
  protected final HttpMethod method;
  protected final String antPattern;
  private final String stringBeforeId;

  protected String getRequestId(HttpServletRequest request) {
    var uriPattern = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    var uriMatcher = uriPattern.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
