package school.hei.api.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import school.hei.api.model.enums.Role;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

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
        Role role = jwtService.extractRole(token);

        // principal name == user id, so @PreAuthorize("#id == authentication.name")
        // works directly in the controllers
        var authentication =
            new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }
}
