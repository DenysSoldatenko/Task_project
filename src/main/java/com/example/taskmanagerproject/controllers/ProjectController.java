package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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
 * Controller responsible for handling project-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/projects")
@Tag(name = "Project Controller", description = "Endpoints for managing projects")
public class ProjectController {

  private final ProjectService projectService;

  /**
   * Creates a new project.
   *
   * @param projectDto the data transfer object containing project details
   * @return the created project
   */
  @PostMapping()
  @Operation(
      summary = "Create a new project",
      description = "Allows users with specific roles to create a new project in the system",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Details of the project to be created", required = true,
        content = @Content(schema = @Schema(implementation = ProjectDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "201", description = "Project created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
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
  @MutationMapping(name = "createProject")
  public ProjectDto createProject(
      @Valid @RequestBody @Argument ProjectDto projectDto
  ) {
    return projectService.createProject(projectDto);
  }

  /**
   * Retrieves a project by its name.
   *
   * @param projectName the name of the project
   * @return the found project, or 404 if the project doesn't exist
   */
  @GetMapping("/{projectName}")
  @Operation(
      summary = "Retrieve a project by name",
      description = "Fetches a project based on its name",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project to retrieve",
          required = true, in = ParameterIn.PATH, example = "Project Alpha")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Project found successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @QueryMapping(name = "getProjectByName")
  public ProjectDto getProjectByName(
      @PathVariable("projectName") @Argument String projectName
  ) {
    return projectService.getProjectByName(projectName);
  }

  /**
   * Retrieves all teams for a specific project.
   *
   * @param projectName the name of the project
   * @return a list of teams associated with the project, or 404 if the project doesn't exist
   */
  @GetMapping("/{projectName}/teams")
  @Operation(
      summary = "Retrieve all teams for a specific project",
      description = "Fetches a list of teams for the given project",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project whose teams should be retrieved",
          required = true, in = ParameterIn.PATH, example = "Project Alpha")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Teams found successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectTeamDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @QueryMapping(name = "getTeamsForProject")
  public List<ProjectTeamDto> getTeamsForProject(
      @PathVariable("projectName") @Argument String projectName
  ) {
    return projectService.getTeamsForProject(projectName);
  }

  /**
   * Updates an existing project.
   *
   * @param projectName the name of the project to update
   * @param projectDto the data transfer object containing the updated project details
   * @return the updated project
   */
  @PutMapping("/{projectName}")
  @PreAuthorize("@expressionService.canAccessProject(#projectName)")
  @Operation(
      summary = "Update an existing project",
      description = "Allows users with specific roles to update an existing project",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project to update",
          required = true, in = ParameterIn.PATH, example = "Project Alpha")
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Updated project information", required = true,
        content = @Content(schema = @Schema(implementation = ProjectDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @MutationMapping(name = "updateProject")
  public ProjectDto updateProject(
      @PathVariable("projectName") @Argument String projectName,
      @Valid @RequestBody @Argument ProjectDto projectDto
  ) {
    return projectService.updateProject(projectName, projectDto);
  }

  /**
   * Deletes a project by its name.
   *
   * @param projectName the name of the project to delete
   */
  @DeleteMapping("/{projectName}")
  @PreAuthorize("@expressionService.canAccessProject(#projectName)")
  @Operation(
      summary = "Delete a project",
      description = "Allows users with specific roles to delete a project",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project to delete",
          required = true, in = ParameterIn.PATH, example = "Project Alpha")
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  @MutationMapping(name = "deleteProject")
  public void deleteProject(
      @PathVariable("projectName") @Argument String projectName
  ) {
    projectService.deleteProject(projectName);
  }

  /**
   * Adds a team to a specific project, assigning roles.
   *
   * @param projectName the name of the project to which the team will be added
   * @param projectTeamDtoList the list of team-project-role associations to be added
   * @return the updated project after the team has been added
   */
  @PostMapping("/{projectName}/teams")
  @PreAuthorize("@expressionService.canAccessProject(#projectName)")
  @Operation(
      summary = "Add team to a project",
      description = "Assigns teams to a project with a specific role",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project to assign the teams to",
          required = true, in = ParameterIn.PATH, example = "Project Alpha")
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "List of teams to assign to the project", required = true,
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectTeamDto.class)))
      ),
      responses = {
        @ApiResponse(responseCode = "201", description = "Team added to project successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectTeamDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Project or team not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  @MutationMapping(name = "addTeamToProject")
  public ProjectDto addTeamToProject(
      @PathVariable("projectName") @Argument String projectName,
      @Valid @RequestBody @Argument List<ProjectTeamDto> projectTeamDtoList
  ) {
    return projectService.addTeamToProject(projectName, projectTeamDtoList);
  }
}
