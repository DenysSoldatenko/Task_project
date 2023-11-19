package com.example.taskmanagerproject.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
public record UserDto(
  Long id,

  @NotNull(message = "Full name cannot be null!")
  @NotBlank(message = "Full name cannot be blank!")
  @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters long!")
  String fullName,

  @NotNull(message = "Username cannot be null!")
  @NotBlank(message = "Username cannot be blank!")
  @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters long!")
  String username,

  @NotNull(message = "Password cannot be null!")
  @NotBlank(message = "Password cannot be blank!")
  @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters long!")
  @ToString.Include(name = "password")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  String password,

  @NotNull(message = "Confirm password cannot be null!")
  @NotBlank(message = "Confirm password cannot be blank!")
  @Size(min = 6, max = 20, message = "Confirm password must be between 6 and 20 characters long!")
  @ToString.Include(name = "confirmPassword")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  String confirmPassword
) {}
