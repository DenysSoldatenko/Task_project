package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationServiceImplTest {

  private final AuthenticationServiceImpl service = new AuthenticationServiceImpl();

  @Test
  void logAuthenticationInfo_shouldReturnUnauthenticatedMessageWhenAuthenticationIsNull() {
    String result = service.logAuthenticationInfo(null);
    assertEquals("User not authenticated.", result);
  }

  @Test
  void logAuthenticationInfo_shouldReturnUnauthenticatedMessageWhenNotAuthenticated() {
    Authentication unauthenticated = mock(Authentication.class);
    when(unauthenticated.isAuthenticated()).thenReturn(false);

    String result = service.logAuthenticationInfo(unauthenticated);
    assertEquals("User not authenticated.", result);
    verify(unauthenticated).isAuthenticated();
  }

  @Test
  void logAuthenticationInfo_shouldReturnJwtAuthenticationInfo() {
    Map<String, Object> claims = Map.of("sub", "user123", "email", "user@example.com", "role", "ADMIN");

    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "HS256")
        .claim("sub", "user123")
        .claims(claimsMap -> claimsMap.putAll(claims))
        .build();

    JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")), "user123");

    String info = service.logAuthenticationInfo(token);

    assertTrue(info.contains("Authenticated user: user123"));
    assertTrue(info.contains("Authentication type: JwtAuthenticationToken"));
    assertTrue(info.contains("JWT Subject: user123"));
    assertTrue(info.contains("email: user@example.com"));
    assertTrue(info.contains("role: ADMIN"));
  }
}
