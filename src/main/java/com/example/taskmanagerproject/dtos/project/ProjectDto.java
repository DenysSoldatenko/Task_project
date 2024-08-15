package com.example.taskmanagerproject.dtos.project;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.example.taskmanagerproject.dtos.security.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a Project DTO for transferring project data.
 */
@Schema(description = "Data Transfer Object representing a project")
public record ProjectDto(

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier of the project",
      hidden = true
    )
    Long id,

    @Schema(
      description = "The name of the project",
      example = "Project Alpha"
    )
    @NotNull(message = "Project name cannot be null")
    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    String name,

    @Schema(
      description = "A brief description of the project",
      example = "This is a description of Project Alpha"
    )
    @NotNull(message = "Project description cannot be null")
    @NotBlank(message = "Project description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @JsonProperty(access = WRITE_ONLY)
    UserDto creator
) {}
