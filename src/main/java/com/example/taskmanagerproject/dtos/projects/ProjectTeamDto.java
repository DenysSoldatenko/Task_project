package com.example.taskmanagerproject.dtos.projects;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object representing the association between a user, a project, and their role.
 */
@Schema(description = "Data Transfer Object representing the project-team association")
public record ProjectTeamDto(

    @Schema(description = "ID of the team the user is being assigned to")
    TeamDto team,

    @Schema(description = "ID of the project the user is being assigned to")
    ProjectDto project
) {}
