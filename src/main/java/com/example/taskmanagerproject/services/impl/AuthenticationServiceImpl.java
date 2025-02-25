package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Handles authentication-related business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  /**
   * Logs detailed authentication information for debugging or auditing.
   *
   * @param authentication the current Spring Security authentication object
   */
  @Override
  public String logAuthenticationInfo(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("No authenticated user found.");
      return "User not authenticated.";
    }

    StringBuilder info = new StringBuilder();
    String username = authentication.getName();
    String authType = authentication.getClass().getSimpleName();

    info.append("Authenticated user: ").append(username).append("\n");
    info.append("Authentication type: ").append(authType).append("\n");

    if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
      Jwt jwt = jwtAuthToken.getToken();
      info.append("JWT Subject: ").append(jwt.getSubject()).append("\n");
      info.append("JWT Claims:\n");
      jwt.getClaims().forEach((key, value) -> info.append("  ").append(key).append(": ").append(value).append("\n"));
    } else {
      String principalType = authentication.getPrincipal().getClass().getSimpleName();
      info.append("Principal class: ").append(principalType).append("\n");
    }

    return info.toString();
  }
}
