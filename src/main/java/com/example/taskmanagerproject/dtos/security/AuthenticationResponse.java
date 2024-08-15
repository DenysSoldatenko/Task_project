package com.example.taskmanagerproject.dtos.security;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for authentication responses.
 */
@Schema(description = "Response containing the authentication token")
public record AuthenticationResponse(

    @Schema(
      description = "The authentication token",
      example = "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
    )
    String token
) {}
