package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
public class AuthenticationInfoController {

  private final AuthenticationService authenticationService;

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
  @ResponseStatus(HttpStatus.OK)
  public String getUserInfo(Authentication authentication) {
    return authenticationService.logAuthenticationInfo(authentication);
  }
}
