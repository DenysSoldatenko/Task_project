package com.example.taskmanagerproject.services;

import org.springframework.security.core.Authentication;

/**
 * Service interface for handling authentication-related operations.
 */
public interface AuthenticationService {

  /**
   * Logs and returns a textual summary of the currently authenticated user's identity and credentials.
   *
   * @param authentication the Spring Security {@link Authentication} object representing the current user.
   * @return a formatted string containing user identity and token claims; or a message indicating no user is authenticated.
   */
  String logAuthenticationInfo(Authentication authentication);
}
