package com.example.taskmanagerproject.dtos;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
@Schema(description = "Data Transfer Object representing a user")
public record UserDto(

    @Schema(
      description = "The unique identifier of the user",
      hidden = true
    )
    Long id,

    @NotNull(message = "Full name cannot be null!")
    @NotBlank(message = "Full name cannot be blank!")
    @Size(
      min = MIN_FULL_NAME_LENGTH,
      max = MAX_FULL_NAME_LENGTH,
      message = "Full name must be between " + MIN_FULL_NAME_LENGTH
        + " and " + MAX_FULL_NAME_LENGTH + " characters long!"
    )
    @Schema(
      description = "The full name of the user",
      example = "Alice Johnson",
      maxLength = MAX_FULL_NAME_LENGTH
    )
    String fullName,

    @NotNull(message = "Username cannot be null!")
    @NotBlank(message = "Username cannot be blank!")
    @Size(
      min = MIN_USERNAME_LENGTH,
      max = MAX_USERNAME_LENGTH,
      message = "Username must be between " + MIN_USERNAME_LENGTH
        + " and " + MAX_USERNAME_LENGTH + " characters long!"
    )
    @Schema(
      description = "The username of the user",
      example = "alice123@gmail.com",
      maxLength = MAX_USERNAME_LENGTH
    )
    String username,

    @NotNull(message = "Password cannot be null!")
    @NotBlank(message = "Password cannot be blank!")
    @Size(
      min = MIN_PASSWORD_LENGTH,
      max = MAX_PASSWORD_LENGTH,
      message = "Password must be between " + MIN_PASSWORD_LENGTH
        + " and " + MAX_PASSWORD_LENGTH + " characters long!"
    )
    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "The password of the user",
      example = "password123",
      maxLength = MAX_PASSWORD_LENGTH
    )
    String password,

    @NotNull(message = "Confirm password cannot be null!")
    @NotBlank(message = "Confirm password cannot be blank!")
    @Size(
      min = MIN_PASSWORD_LENGTH,
      max = MAX_PASSWORD_LENGTH,
      message = "Confirm password must be between " + MIN_PASSWORD_LENGTH
        + " and " + MAX_PASSWORD_LENGTH + " characters long!"
    )
    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "The confirmation password of the user",
      example = "password123",
      maxLength = MAX_PASSWORD_LENGTH
    )
    String confirmPassword,

    @JsonProperty(access = WRITE_ONLY)
    RoleDto role
) implements Serializable {

  public static final int MIN_FULL_NAME_LENGTH = 2;
  public static final int MAX_FULL_NAME_LENGTH = 50;
  public static final int MIN_USERNAME_LENGTH = 4;
  public static final int MAX_USERNAME_LENGTH = 25;
  public static final int MIN_PASSWORD_LENGTH = 6;
  public static final int MAX_PASSWORD_LENGTH = 20;
}
