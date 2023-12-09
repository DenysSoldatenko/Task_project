package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<AuthenticationResponse> register(
      @Valid @RequestBody final UserDto request
  ) {
    return ResponseEntity.ok(authenticationService.registerUser(request));
  }

  @PostMapping("/authenticate")
  @Operation(
      summary = "Authenticate a user",
      description = "Authenticate a user with the provided credentials"
  )
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Valid @RequestBody final AuthenticationRequest request
  ) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }
}
