package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Provides utility methods for working with JWT tokens.
 */
@Data
@Component
public class JwtTokenProvider {

  private final UserDetailsService userDetailsService;

  @Value("${jwt.token.secret}")
  private String secret;

  @Value("${jwt.token.expired}")
  private long validityInMilliseconds;

  @Autowired
  public JwtTokenProvider(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @PostConstruct
  protected void init() {
    secret = Base64.getEncoder().encodeToString(secret.getBytes());
  }

  /**
   * Creates a JWT token for the given username and roles.
   *
   * @param username The username for which the token is created.
   * @param roles    The roles associated with the user.
   * @return A JWT token as a String.
   */
  public String createToken(String username, List<Role> roles) {

    Claims claims = Jwts.claims().setSubject(username);
    claims.put("roles", getRoleNames(roles));

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
    .setClaims(claims)
    .setIssuedAt(now)
    .setExpiration(validity)
    .signWith(SignatureAlgorithm.HS256, secret)
    .compact();
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  /**
   * Retrieves the username from a JWT token.
   *
   * @param token The JWT token as a String.
   * @return The username extracted from the token.
   */
  public String getUsername(String token) {
    return Jwts.parser()
    .setSigningKey(secret)
    .parseClaimsJws(token)
    .getBody()
    .getSubject();
  }

  /**
   * Resolves a JWT token from the request.
   *
   * @param req The HttpServletRequest object.
   * @return The JWT token as a String, or null if not found.
   */
  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer_")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  @SneakyThrows({JwtException.class, IllegalArgumentException.class})
  public boolean validateToken(String token) {
    Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
    return !claims.getBody().getExpiration().before(new Date());
  }

  private List<String> getRoleNames(List<Role> userRoles) {
    return userRoles.stream().map(Role::name).toList();
  }
}