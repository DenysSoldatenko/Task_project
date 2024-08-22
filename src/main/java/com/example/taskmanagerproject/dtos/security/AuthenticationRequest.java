package com.example.taskmanagerproject.dtos.security;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for authentication requests.
 */
@Schema(description = "Authentication request data")
public record AuthenticationRequest(

    @Schema(
      description = "Username for authentication",
      example = "alice12345@gmail.com"
    )
    String username,

    @Schema(
      description = "Password for authentication",
      example = "password123"
    )
    String password
) {}
