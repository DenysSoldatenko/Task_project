package com.example.taskmanagerproject.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
public record UserDto(

    @Schema(description = "The unique identifier of the user", hidden = true)
    Long id,

    @NotNull(message = "Full name cannot be null!")
    @NotBlank(message = "Full name cannot be blank!")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters long!")
    @Schema(
      description = "The full name of the user",
      example = "Alice Johnson",
      maxLength = 50
    )
    String fullName,

    @NotNull(message = "Username cannot be null!")
    @NotBlank(message = "Username cannot be blank!")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters long!")
    @Schema(
      description = "The username of the user",
      example = "alice123@gmail.com",
      maxLength = 20
    )
    String username,

    @NotNull(message = "Password cannot be null!")
    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters long!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(
      description = "The password of the user",
      example = "password123",
      maxLength = 20
    )
    String password,

    @NotNull(message = "Confirm password cannot be null!")
    @NotBlank(message = "Confirm password cannot be blank!")
    @Size(min = 6, max = 20, message = "Confirm password must be between 6 and 20 characters long!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(
      description = "The confirmation password of the user",
      example = "password123",
      maxLength = 20
    )
    String confirmPassword
) {}
