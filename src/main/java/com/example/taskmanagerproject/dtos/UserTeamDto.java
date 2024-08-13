package com.example.taskmanagerproject.dtos;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object representing the association between a user, a team, and their role.
 */
@Schema(description = "Data Transfer Object representing the user-team-role association")
public record UserTeamDto(

    @Schema(
      description = "ID of the user who is being assigned to the team",
      example = "1"
    )
    Long userId,

    @JsonProperty(access = WRITE_ONLY)
    @Schema(
      description = "ID of the team the user is being assigned to",
      example = "1"
    )
    Long teamId,

    @Schema(
      description = "ID of the role the user holds within the team",
      example = "1"
    )
    Long roleId
) {}
