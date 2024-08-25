package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling user-related endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class UserController {

  private final UserService userService;
  private final TaskService taskService;
  private final TeamService teamService;
  private final ProjectService projectService;

  /**
   * Retrieves user information by slug.
   *
   * @param slug The slug of the user to retrieve.
   * @return ResponseEntity containing the user DTO.
   */
  @GetMapping("/{slug}")
  @Operation(
      summary = "Get user by slug",
      description = "Retrieve user information by slug",
      responses = {
          @ApiResponse(responseCode = "200", description = "User retrieved successfully",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @QueryMapping(name = "getUserBySlug")
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public UserDto getUserBySlug(@PathVariable(name = "slug") @Argument String slug) {
    return userService.getUserBySlug(slug);
  }

  /**
   * Updates user information by slug.
   *
   * @param userDto The DTO containing the updated user information.
   * @param slug    The slug of the user to update.
   * @return ResponseEntity containing the updated user DTO.
   */
  @PutMapping("/{slug}")
  @Operation(
      summary = "Update user",
      description = "Update user information by slug",
      responses = {
          @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @MutationMapping(name = "updateUser")
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public UserDto updateUser(
      @Valid @RequestBody @Argument UserDto userDto,
      @PathVariable(name = "slug") @Argument String slug
  ) {
    return userService.updateUser(userDto, slug);
  }

  /**
   * Deletes a user by slug.
   *
   * @param slug The slug of the user to delete.
   */
  @DeleteMapping("/{slug}")
  @Operation(
      summary = "Delete user by slug",
      description = "Delete user by slug",
      responses = {
          @ApiResponse(responseCode = "204", description = "User deleted successfully"),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(NO_CONTENT)
  @MutationMapping(name = "deleteUserBySlug")
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public void deleteUserBySlug(@PathVariable(name = "slug") @Argument String slug) {
    userService.deleteUserBySlug(slug);
  }

  /**
   * Retrieves projects associated with a user based on the user's slug.
   *
   * @param slug the unique identifier (slug) of the user
   * @return a list of projects associated with the specified user
   * @throws UserNotFoundException if the user is not found
   */
  @GetMapping("/{slug}/projects")
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  @Operation(
      summary = "Get projects by user username",
      description = "Fetches all projects associated with a user identified by the username",
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved projects",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProjectDto.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  public List<ProjectDto> getProjectsByUserSlug(@PathVariable(name = "slug") String slug) {
    return projectService.getProjectsBySlug(slug);
  }

  /**
   * Retrieves teams associated with a user based on the user's slug.
   *
   * @param slug the unique identifier (slug) of the user
   * @return a list of teams associated with the specified user
   * @throws UserNotFoundException if the user is not found
   */
  @GetMapping("/{slug}/teams")
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  @Operation(
      summary = "Get teams by user username",
      description = "Fetches all teams associated with a user identified by the username",
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved teams",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ProjectDto.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  public List<TeamDto> getTeamsByUserSlug(@PathVariable(name = "slug") String slug) {
    return teamService.getTeamsBySlug(slug);
  }

  /**
   * Retrieves tasks assigned to a user by ID.
   *
   * @param id The ID of the user to retrieve tasks for.
   * @return ResponseEntity containing a list of task DTOs.
   */
  @GetMapping("/{id}/tasks/assigned-to")
  @Operation(
      summary = "Get tasks assigned to a user",
      description = "Retrieve tasks assigned to a user by ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TaskDto[].class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @QueryMapping(name = "getTasksAssignedToUser")
  @PreAuthorize("@expressionService.canAccessUserDataById(#id)")
  public List<TaskDto> getTasksAssignedToUser(@PathVariable(name = "id") @Argument Long id) {
    return taskService.getAllTasksAssignedToUser(id);
  }

  /**
   * Retrieves tasks assigned by a user by ID.
   *
   * @param id The ID of the user to retrieve tasks for.
   * @return ResponseEntity containing a list of task DTOs.
   */
  @GetMapping("/{id}/tasks/assigned-by")
  @Operation(
      summary = "Get tasks assigned by a user",
      description = "Retrieve tasks assigned by a user by ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TaskDto[].class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @QueryMapping(name = "getTasksAssignedByUser")
  @PreAuthorize("@expressionService.canAccessUserDataById(#id)")
  public List<TaskDto> getTasksAssignedByUser(@PathVariable(name = "id") @Argument Long id) {
    return taskService.getAllTasksAssignedByUser(id);
  }
}
