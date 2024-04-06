package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling authentication-related operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication Controller",
    description = "Endpoints for user authentication"
)
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description = "Register a new user with the provided data"
  )
  @ResponseStatus(OK)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User registered successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = AuthenticationResponse.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input data",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorDetails.class))
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorDetails.class))
      )
  })
  public AuthenticationResponse register(
      @Valid @RequestBody final UserDto request
  ) {
    return authenticationService.registerUser(request);
  }

  @PostMapping("/authenticate")
  @Operation(
      summary = "Authenticate a user",
      description = "Authenticate a user with the provided credentials"
  )
  @ResponseStatus(OK)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authentication successful",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = AuthenticationResponse.class))
      ),
      @ApiResponse(responseCode = "401", description = "Invalid credentials",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorDetails.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input data",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorDetails.class))
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorDetails.class))
      )
  })
  public AuthenticationResponse authenticate(
      @Valid @RequestBody final AuthenticationRequest request
  ) {
    return authenticationService.authenticate(request);
  }
}
