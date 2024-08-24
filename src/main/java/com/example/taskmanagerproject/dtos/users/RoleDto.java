package com.example.taskmanagerproject.dtos.users;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for representing Role information.
 * Used for API requests and responses.
 */
@Schema(description = "Data Transfer Object representing a role")
public record RoleDto(

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier of the role",
      hidden = true
    )
    Long id,

    @NotNull(message = "Role name cannot be null!")
    @NotBlank(message = "Role name cannot be blank!")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters long!")
    @Pattern(
      regexp = "^[a-zA-Z0-9_]+$",
      message = "Role name must be alphanumeric and may contain underscores"
    )
    @Schema(
      description = "The name of the role (e.g., ADMIN, USER)",
      example = "TESTER"
    )
    String name,

    @NotNull(message = "Role description cannot be null!")
    @NotBlank(message = "Role description cannot be blank!")
    @Size(min = 2, max = 200, message = "Description must be between 2 and 200 characters long!")
    @Schema(
      description = "A brief description of the role",
      example = "Tester responsible for verifying the tasks"
    )
    String description
) {}
