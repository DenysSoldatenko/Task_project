package com.example.taskmanagerproject.dtos.security;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
@Schema(description = "Data Transfer Object representing a user")
public record UserDto(

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier of the user",
      hidden = true
    )
    Long id,

    @NotNull(message = "Full name cannot be null!")
    @NotBlank(message = "Full name cannot be blank!")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters long!")
    @Schema(
      description = "The full name of the user",
      example = "Alice Johnson"
    )
    String fullName,

    @NotNull(message = "Username cannot be null!")
    @NotBlank(message = "Username cannot be blank!")
    @Size(min = 10, max = 55, message = "Username must be between 10 and 55 characters long!")
    @Schema(
      description = "The username of the user",
      example = "alice123@gmail.com"
    )
    String username,

    @NotNull(message = "Password cannot be null!")
    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 6, max = 25, message = "Password must be between 6 and 25 characters long!")
    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "The password of the user",
      example = "password123"
    )
    String password,

    @NotNull(message = "Confirm password cannot be null!")
    @NotBlank(message = "Confirm password cannot be blank!")
    @Size(min = 6, max = 25, message = "Confirm password must be between 6 and 25 characters long!")
    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "The confirmation password of the user",
      example = "password123"
    )
    String confirmPassword,

    @JsonProperty(access = WRITE_ONLY)
    RoleDto role
) {}
