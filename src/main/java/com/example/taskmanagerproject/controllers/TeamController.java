package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling team-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/teams")
@Tag(name = "Team Controller", description = "Endpoints for managing teams")
public class TeamController {

  private final TeamService teamService;
  private final ProjectService projectService;

  /**
   * Creates a new team.
   *
   * @param teamDto the data transfer object containing team details
   * @return the created team
   */
  @PostMapping()
  @Operation(
      summary = "Create a new team",
      description = "Allows users with specific roles to create a new team in the system",
      responses = {
        @ApiResponse(responseCode = "201", description = "Team created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public TeamDto createTeam(@RequestBody @Valid TeamDto teamDto) {
    return teamService.createTeam(teamDto);
  }

  /**
   * Retrieves a team by its name.
   *
   * @param teamName the name of the team
   * @return the found team, or 404 if the team doesn't exist
   */
  @GetMapping("/{teamName}")
  @Operation(
      summary = "Retrieve a team by name",
      description = "Fetches a team based on its name",
      responses = {
        @ApiResponse(responseCode = "200", description = "Team found successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public TeamDto getTeamByName(@PathVariable("teamName") String teamName) {
    return teamService.getTeamByName(teamName);
  }

  /**
   * Retrieves all users and their roles for a specific team.
   *
   * @param teamName the name of the team
   * @return a list of users and their roles in the team, or 404 if the team doesn't exist
   */
  @GetMapping("/{teamName}/users-roles")
  @Operation(
      summary = "Retrieve all users and their roles for a specific team",
      description = "Fetches a list of users and their roles for the given team",
      responses = {
        @ApiResponse(responseCode = "200", description = "Users and roles found successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamUserDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public List<TeamUserDto> getUsersWithRolesForTeam(@PathVariable("teamName") String teamName) {
    return teamService.getUsersWithRolesForTeam(teamName);
  }

  /**
   * Retrieves all projects for a specific team.
   *
   * @param teamName the name of the team
   * @return a list of projects associated with the team, or 404 if the team doesn't exist
   */
  @GetMapping("/{teamName}/projects")
  @Operation(
      summary = "Retrieve all projects for a specific team",
      description = "Fetches a list of projects associated with the given team",
      responses = {
        @ApiResponse(responseCode = "200", description = "Projects found successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public List<ProjectTeamDto> getProjectsForTeam(@PathVariable("teamName") String teamName) {
    return projectService.getProjectsForTeam(teamName);
  }

  /**
   * Updates an existing team.
   *
   * @param teamName the name of the team to update
   * @param teamDto  the data transfer object containing the updated team details
   * @return the updated team
   */
  @PutMapping("/{teamName}")
  @PreAuthorize("@expressionService.canAccessTeam(#teamName)")
  @Operation(
      summary = "Update an existing team",
      description = "Allows users with specific roles to update an existing team",
      responses = {
        @ApiResponse(responseCode = "200", description = "Team updated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public TeamDto updateTeam(@PathVariable("teamName") String teamName, @RequestBody @Valid TeamDto teamDto) {
    return teamService.updateTeam(teamName, teamDto);
  }

  /**
   * Deletes a team by its name.
   *
   * @param teamName the name of the team to delete
   */
  @DeleteMapping("/{teamName}")
  @PreAuthorize("@expressionService.canAccessTeam(#teamName)")
  @Operation(
      summary = "Delete a team",
      description = "Allows users with specific roles to delete a team",
      responses = {
        @ApiResponse(responseCode = "204", description = "Team deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  public void deleteTeam(@PathVariable("teamName") String teamName) {
    teamService.deleteTeam(teamName);
  }

  /**
   * Adds a list of users to a specific team, assigning them roles.
   *
   * @param teamName the name of the team to which the users will be added
   * @param teamUserDtoList the list of user-team-role associations to be added
   * @return the updated team after the users have been added
   */
  @PostMapping("/{teamName}/users")
  @PreAuthorize("@expressionService.canAccessTeam(#teamName)")
  @Operation(
      summary = "Add users to a team",
      description = "Assigns users to a team with a specific role",
      responses = {
        @ApiResponse(responseCode = "201", description = "Users added to team successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamUserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User, team, or role not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public List<TeamUserDto> addUsersToTeam(@PathVariable("teamName") String teamName, @RequestBody @Valid List<TeamUserDto> teamUserDtoList) {
    return teamService.addUsersToTeam(teamName, teamUserDtoList);
  }
}
