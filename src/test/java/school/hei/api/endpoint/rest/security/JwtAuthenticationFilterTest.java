package school.hei.api.endpoint.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import school.hei.api.model.enums.Role;

class JwtAuthenticationFilterTest {

  private final JwtService jwtService = mock(JwtService.class);
  private final JwtAuthenticationFilter subject = new JwtAuthenticationFilter(jwtService);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final FilterChain filterChain = mock(FilterChain.class);

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void valid_token_sets_authentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
    when(jwtService.isValid("valid-token")).thenReturn(true);
    when(jwtService.extractUserId("valid-token")).thenReturn("user-id-1");
    when(jwtService.extractRole("valid-token")).thenReturn(Role.ADMIN);

    subject.doFilter(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals("user-id-1", authentication.getPrincipal());
    assertEquals(1, authentication.getAuthorities().size());
    assertEquals("ROLE_ADMIN", authentication.getAuthorities().iterator().next().getAuthority());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void invalid_token_does_not_set_authentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(jwtService.isValid("invalid-token")).thenReturn(false);

    subject.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void missing_header_does_not_set_authentication() throws Exception {
    subject.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void non_bearer_header_does_not_set_authentication() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic abc");

    subject.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void authentication_is_set_only_for_valid_token() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwtService.isValid("token")).thenReturn(true);
    when(jwtService.extractUserId("token")).thenReturn("u1");
    when(jwtService.extractRole("token")).thenReturn(Role.STUDENT);

    subject.doFilter(request, response, filterChain);
    assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
  }
}
