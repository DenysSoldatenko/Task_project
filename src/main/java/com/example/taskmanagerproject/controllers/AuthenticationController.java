package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.AuthenticationService;
import com.example.taskmanagerproject.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling authentication-related requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication")
public class AuthenticationController {

  private final UserService userService;
  private final AuthenticationService authenticationService;

  /**
   * Creates a new user from JWT authentication token.
   *
   * @param jwtAuth The JWT authentication token (injected automatically)
   * @return The created UserDto
   */
  @PostMapping()
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Create user from JWT token",
      description = "Creates a new user in the system based on the JWT token claims",
      responses = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid or missing claims in JWT",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public UserDto registerAuthenticatedUser(@Parameter(hidden = true) JwtAuthenticationToken jwtAuth) {
    return userService.createUser(jwtAuth);
  }

  /**
   * Returns detailed information about the currently authenticated user.
   *
   * @param authentication the Spring Security authentication object
   * @return a string representation of user identity and claims
   */
  @GetMapping("/user-info")
  @Operation(
      summary = "Get logged-in user info",
      description = "Returns identity and JWT claim details of the currently authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
          content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public String getCurrentUserProfile(@Parameter(hidden = true) Authentication authentication) {
    return authenticationService.logAuthenticationInfo(authentication);
  }
}
