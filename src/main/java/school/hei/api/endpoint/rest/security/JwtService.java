package school.hei.api.endpoint.rest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import school.hei.api.model.User;
import school.hei.api.model.enums.Role;

@Component
public class JwtService {

  private final SecretKey key;
  private final long expirationSeconds;

  public JwtService(
      @Value("${school.hei.api.jwt.secret}") String secret,
      @Value("${school.hei.api.jwt.expiration-seconds:3600}") long expirationSeconds) {
    // secret must be at least 32 chars (256 bits) for HS256
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  public long getExpirationSeconds() {
    return expirationSeconds;
  }

  public String generateToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expirationSeconds)))
        .signWith(key)
        .compact();
  }

  /** Returns the user id (JWT subject), or throws JwtException if invalid/expired. */
  public String extractUserId(String token) {
    return parseClaims(token).getSubject();
  }

  public Role extractRole(String token) {
    return Role.valueOf(parseClaims(token).get("role", String.class));
  }

  public boolean isValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}