package com.example.taskmanagerproject.security;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.claims;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

import com.example.taskmanagerproject.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service class for managing JWT tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final UserDetailsService userDetailsService;

  @Value("${jwt.token.secret}")
  private String secret;

  @Value("${jwt.token.expired}")
  private long validityInMilliseconds;

  private SecretKey key;

  @PostConstruct
  public void init() {
    this.key = hmacShaKeyFor(secret.getBytes());
  }

  /**
   * Creates an access token for the specified user.
   *
   * @param userId   The ID of the user.
   * @param username The username of the user.
   * @param roles    The roles assigned to the user.
   * @return The generated access token.
   */
  public String createAccessToken(
      final Long userId,
      final String username,
      final Set<Role> roles
  ) {

    Claims claims = claims()
        .subject(username)
        .add("id", userId)
        .add("roles", resolveRoles(roles))
        .build();

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return builder()
      .claims(claims)
      .expiration(validity)
      .signWith(key)
      .compact();
  }

  private List<String> resolveRoles(final Set<Role> roles) {
    return roles.stream()
      .map(Enum::name)
      .toList();
  }

  /**
   * Validates the provided JWT token.
   *
   * @param token The JWT token to validate.
   * @return true if the token is valid, false otherwise.
   */
  public boolean validateToken(final String token) {
    Jws<Claims> claims = parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token);
    return claims.getPayload().getExpiration().after(new Date());
  }

  private String getUsername(final String token) {
    return parser()
      .verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .getPayload()
      .getSubject();
  }

  /**
   * Retrieves the authentication information from the provided JWT token.
   *
   * @param token The JWT token.
   * @return The authentication information extracted from the token.
   */
  public Authentication getAuthentication(final String token) {
    String username = getUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }
}
