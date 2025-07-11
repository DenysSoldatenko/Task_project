package com.example.taskmanagerproject.dtos.teams;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a Team DTO for transferring team data.
 */
@Schema(description = "Data Transfer Object representing a team")
public record TeamDto(

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier of the team",
      hidden = true
    )
    Long id,

    @Schema(
      description = "The name of the team",
      example = "Team Alpha"
    )
    @NotNull(message = "Team name cannot be null")
    @NotBlank(message = "Team name cannot be blank")
    @Size(min = 3, max = 50, message = "Team name must be between 3 and 100 characters")
    String name,

    @Schema(
      description = "A brief description of the team",
      example = "This is a description of Team A"
    )
    @NotNull(message = "Team description cannot be null")
    @NotBlank(message = "Team description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @JsonProperty(access = WRITE_ONLY)
    UserDto creator
) {}
