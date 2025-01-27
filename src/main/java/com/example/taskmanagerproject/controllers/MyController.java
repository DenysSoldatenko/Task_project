package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
// todo: move to auth
@RestController
public class MyController {

  private static final Logger logger = LoggerFactory.getLogger(MyController.class);

    private final UserService userService;

  public MyController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/user-info")
  public void getUserInfo(Authentication authentication) {
      if (authentication != null && authentication.isAuthenticated()) {
          logger.info("User {} is logged in via Keycloak.", authentication.getName());

          // --- Option 1: Using OAuth2AuthenticationToken (for OIDC clients) ---
          if (authentication instanceof OAuth2AuthenticationToken) {
              OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
              if (oauthToken.getPrincipal() instanceof OidcUser) {
                  OidcUser oidcUser = (OidcUser) oauthToken.getPrincipal();
                  Map<String, Object> claims = oidcUser.getClaims(); // Get all claims from OIDC user

                  logger.info("Keycloak OIDC User Claims:");
                  claims.forEach((key, value) -> logger.info("  {}: {}", key, value));

                  System.out.println( "Logged in as OIDC user: " + oidcUser.getFullName() + ". Attributes logged.");
              }
          }
          // --- Option 2: Using JwtAuthenticationToken (for resource servers with JWT) ---
          if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken(); // Get the JWT token

              logger.info("Keycloak JWT Token Attributes:");
              jwt.getClaims().forEach((key, value) -> logger.info("  {}: {}", key, value));

              System.out.println( "Logged in via JWT: " + jwt.getSubject() + ". Attributes logged.");
          } else {
              logger.info("Authentication type: {}", authentication.getClass().getName());
              logger.info("Principal: {}", authentication.getPrincipal().getClass().getName());
              // Fallback for other authentication types
              System.out.println("Logged in as: " + authentication.getName()
                + ". Cannot display all attributes for this authentication type.");
          }

      } else {
          logger.warn("No user is currently logged in.");
          System.out.println("Not logged in.");
      }
  }
}