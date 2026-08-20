package school.hei.api.endpoint.rest.security;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.api.model.enums.Role.ADMIN;
import static school.hei.api.model.enums.Role.STUDENT;
import static school.hei.api.model.enums.Role.TEACHER;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import school.hei.api.model.exception.ForbiddenException;
import school.hei.api.repository.GradeRepository;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConf {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final GradeRepository gradeRepository;
  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConf(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      UserDetailsService userDetailsService,
      GradeRepository gradeRepository,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.userDetailsService = userDetailsService;
    this.gradeRepository = gradeRepository;
    this.exceptionResolver = exceptionResolver;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.exceptionHandling(
            exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer
                    .authenticationEntryPoint(
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(req)))
                    .accessDeniedHandler(
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(req))))
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .logout(logout -> logout.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ping", "/health/**", "/graduates", "/auth/login")
                    .permitAll()
                    .requestMatchers(OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(GET, "/whoami")
                    .authenticated()
                    .requestMatchers(GET, "/auth/me")
                    .authenticated()
                    // users
                    .requestMatchers(GET, "/users")
                    .hasRole(ADMIN.name())
                    .requestMatchers(POST, "/users")
                    .hasRole(ADMIN.name())
                    .requestMatchers(PUT, "/users/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(DELETE, "/users/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(new SelfMatcher(GET, "/users/*", "users"))
                    .hasAnyRole(STUDENT.name(), TEACHER.name(), ADMIN.name())
                    .requestMatchers(GET, "/users/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(new SelfMatcher(GET, "/users/*/group_flows", "users"))
                    .hasAnyRole(STUDENT.name(), TEACHER.name(), ADMIN.name())
                    .requestMatchers(GET, "/users/*/group_flows")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    // transcript
                    .requestMatchers(new SelfMatcher(POST, "/users/*/transcript", "users"))
                    .hasAnyRole(STUDENT.name(), TEACHER.name(), ADMIN.name())
                    .requestMatchers(POST, "/users/*/transcript")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    // grades
                    .requestMatchers(GET, "/grades")
                    .hasAnyRole(STUDENT.name(), TEACHER.name(), ADMIN.name())
                    .requestMatchers(
                        new GradeSelfMatcher(GET, "/grades/*", "grades", gradeRepository))
                    .hasRole(STUDENT.name())
                    .requestMatchers(GET, "/grades/*")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    .requestMatchers(
                        new GradeSelfMatcher(GET, "/grades/*/history", "grades", gradeRepository))
                    .hasRole(STUDENT.name())
                    .requestMatchers(GET, "/grades/*/history")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    .requestMatchers(PUT, "/grades/*")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    .requestMatchers(POST, "/grades")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    // exams
                    .requestMatchers(GET, "/exams")
                    .authenticated()
                    .requestMatchers(GET, "/exams/*")
                    .authenticated()
                    .requestMatchers(POST, "/exams")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    .requestMatchers(PUT, "/exams/*")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    .requestMatchers(DELETE, "/exams/*")
                    .hasRole(ADMIN.name())
                    // graduates
                    .requestMatchers(GET, "/promotions/*/graduates")
                    .hasRole(ADMIN.name())
                    .requestMatchers(GET, "/promotions/*/graduates/export")
                    .hasRole(ADMIN.name())
                    // promotions
                    .requestMatchers(GET, "/promotions")
                    .authenticated()
                    .requestMatchers(GET, "/promotions/*")
                    .authenticated()
                    .requestMatchers(POST, "/promotions")
                    .hasRole(ADMIN.name())
                    .requestMatchers(PUT, "/promotions/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(DELETE, "/promotions/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(GET, "/promotions/*/students")
                    .hasAnyRole(TEACHER.name(), ADMIN.name())
                    // course assignments
                    .requestMatchers(GET, "/course_assignments")
                    .authenticated()
                    .requestMatchers(GET, "/course_assignments/*")
                    .authenticated()
                    .requestMatchers(POST, "/course_assignments")
                    .hasRole(ADMIN.name())
                    .requestMatchers(PUT, "/course_assignments/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(DELETE, "/course_assignments/*")
                    .hasRole(ADMIN.name())
                    // courses
                    .requestMatchers(GET, "/courses")
                    .authenticated()
                    .requestMatchers(GET, "/courses/*")
                    .authenticated()
                    .requestMatchers(POST, "/courses")
                    .hasRole(ADMIN.name())
                    .requestMatchers(PUT, "/courses/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(DELETE, "/courses/*")
                    .hasRole(ADMIN.name())
                    // groups
                    .requestMatchers(GET, "/groups")
                    .authenticated()
                    .requestMatchers(GET, "/groups/*")
                    .authenticated()
                    .requestMatchers(POST, "/groups")
                    .hasRole(ADMIN.name())
                    .requestMatchers(PUT, "/groups/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(DELETE, "/groups/*")
                    .hasRole(ADMIN.name())
                    .requestMatchers(POST, "/groups/*/flows")
                    .hasRole(ADMIN.name())
                    .requestMatchers("/**")
                    .denyAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private Exception forbiddenWithRemoteInfo(HttpServletRequest req) {
    log.info(
        "Access is denied for remote caller: address={}, host={}, port={}",
        req.getRemoteAddr(),
        req.getRemoteHost(),
        req.getRemotePort());
    return new ForbiddenException("Access is denied");
  }
}
