package com.example.taskmanagerproject.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) for representing Role information.
 * Used for API requests and responses.
 */
@Schema(description = "Data Transfer Object representing a role")
public record RoleDto(

    @Schema(
      description = "The unique identifier of the role",
      hidden = true
    )
    Long id,

    @NotNull(message = "Role name cannot be null!")
    @NotBlank(message = "Role name cannot be blank!")
    @Size(
      min = MIN_ROLE_NAME_LENGTH,
      max = MAX_ROLE_NAME_LENGTH,
      message = "Role name must be between 2 and 50 characters long!"
    )
    @Pattern(
      regexp = "^[a-zA-Z0-9_]+$",
      message = "Role name must be alphanumeric and may contain underscores."
    )
    @Schema(
      description = "The name of the role (e.g., ADMIN, USER)",
      example = "TESTER",
      maxLength = MAX_ROLE_NAME_LENGTH
    )
    String name,

    @NotNull(message = "Role description cannot be null!")
    @NotBlank(message = "Role description cannot be blank!")
    @Size(
      min = MIN_ROLE_DESCRIPTION_LENGTH,
      max = MAX_ROLE_DESCRIPTION_LENGTH,
      message = "Role description must be between 2 and 200 characters long!"
    )
    @Schema(
      description = "A brief description of the role",
      example = "Tester responsible for verifying the tasks",
      maxLength = MAX_ROLE_DESCRIPTION_LENGTH
    )
    String description
) implements Serializable {

  private static final int MIN_ROLE_NAME_LENGTH = 2;
  private static final int MAX_ROLE_NAME_LENGTH = 50;
  private static final int MIN_ROLE_DESCRIPTION_LENGTH = 2;
  private static final int MAX_ROLE_DESCRIPTION_LENGTH = 200;
}
