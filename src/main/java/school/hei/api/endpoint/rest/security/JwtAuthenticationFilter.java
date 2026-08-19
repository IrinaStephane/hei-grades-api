package school.hei.api.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import school.hei.api.endpoint.rest.security.model.Principal;
import school.hei.api.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX.length());

      if (jwtService.isValid(token)) {
        String userId = jwtService.extractUserId(token);
        userRepository
            .findById(userId)
            .ifPresent(
                user -> {
                  var principal = new Principal(user, token);
                  var authentication =
                      new UsernamePasswordAuthenticationToken(
                          principal, token, principal.getAuthorities());
                  SecurityContextHolder.getContext().setAuthentication(authentication);
                });
      }
    }

    filterChain.doFilter(request, response);
  }
}
