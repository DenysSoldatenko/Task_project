package com.example.taskmanagerproject.controllers;

import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.fromString;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling user-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
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
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @QueryMapping(name = "getUserBySlug")
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
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
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
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
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
   */
  @GetMapping("/{slug}/projects")
  @Operation(
      summary = "Get projects by user username",
      description = "Fetches all projects associated with a user identified by the username",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
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
   */
  @GetMapping("/{slug}/teams")
  @Operation(
      summary = "Get teams by user username",
      description = "Fetches all teams associated with a user identified by the username",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved teams",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public List<TeamDto> getTeamsByUserSlug(@PathVariable(name = "slug") String slug) {
    return teamService.getTeamsBySlug(slug);
  }

  /**
   * Retrieves tasks assigned to a specific user for a specific project and team.
   *
   * @param slug The user's unique identifier.
   * @param projectName The name of the project.
   * @param teamName The name of the team.
   * @param page Page number (0-based).
   * @param size Number of tasks per page.
   * @param sort Sort criteria (for example, "id,asc").
   * @return A paginated list of tasks assigned to the user.
   */
  @GetMapping("/{slug}/tasks/assigned-to")
  @Operation(summary = "Get tasks assigned to a user for a project and team",
      description = "Retrieve tasks assigned to a user by slug, project, and team with pagination",
      responses = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto[].class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public Page<TaskDto> getTasksAssignedToUser(
      @PathVariable String slug,
      @RequestParam(defaultValue = "Project Alpha") String projectName,
      @RequestParam(defaultValue = "Team A") String teamName,
      @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)", example = "0") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Number of tasks per page", example = "10") int size,
      @RequestParam(defaultValue = "id,asc") @Parameter(description = "Sort criteria", example = "id,asc") String sort
  ) {
    String[] sortParams = sort.split(",");
    Direction direction = fromString(sortParams[1]);
    Pageable pageable = of(page, size, by(direction, sortParams[0]));
    return taskService.getAllTasksAssignedToUser(slug, projectName, teamName, pageable);
  }

  /**
   * Retrieves tasks assigned by a specific user for a specific project and team.
   *
   * @param slug The user's unique identifier.
   * @param projectName The name of the project.
   * @param teamName The name of the team.
   * @param page Page number (0-based).
   * @param size Number of tasks per page.
   * @param sort Sort criteria (for example, "id,asc").
   * @return A paginated list of tasks assigned by the user.
   */
  @GetMapping("/{slug}/tasks/assigned-by")
  @Operation(
      summary = "Get tasks assigned by a user for a project and team",
      description = "Retrieve tasks assigned by a user by slug, project, and team with pagination",
      responses = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto[].class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public Page<TaskDto> getTasksAssignedByUser(
      @PathVariable String slug,
      @RequestParam(defaultValue = "Project Alpha") String projectName,
      @RequestParam(defaultValue = "Team A") String teamName,
      @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)", example = "0") int page,
      @RequestParam(defaultValue = "10") @Parameter(description = "Number of tasks per page", example = "10") int size,
      @RequestParam(defaultValue = "id,asc") @Parameter(description = "Sort criteria", example = "id,asc") String sort
  ) {
    String[] sortParams = sort.split(",");
    Direction direction = fromString(sortParams[1]);
    Pageable pageable = of(page, size, by(direction, sortParams[0]));
    return taskService.getAllTasksAssignedByUser(slug, projectName, teamName, pageable);
  }

  /**
   * Uploads a photo for a user.
   *
   * @param imageDto The DTO containing the image to upload.
   * @param slug the unique identifier (slug) of the user.
   */
  @PostMapping("/{slug}/image")
  @Operation(
      summary = "Upload a photo for a user",
      description = "Upload a photo for the user identified by their ID",
      responses = {
        @ApiResponse(responseCode = "204", description = "Photo uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public void uploadUserPhoto(@Valid @ModelAttribute UserImageDto imageDto, @PathVariable(name = "slug") String slug) {
    userService.uploadUserPhoto(slug, imageDto);
  }

  /**
   * Updates a photo for a user.
   *
   * @param imageDto The DTO containing the image to upload.
   * @param slug     The unique identifier (slug) of the user.
   */
  @PutMapping("/{slug}/image")
  @Operation(
      summary = "Update a photo for a user",
      description = "Update the photo for the user identified by their ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Photo updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public void updateUserPhoto(@Valid @ModelAttribute UserImageDto imageDto, @PathVariable(name = "slug") String slug) {
    userService.updateUserPhoto(slug, imageDto);
  }

  /**
   * Deletes a photo for a user.
   *
   * @param slug The unique identifier (slug) of the user.
   */
  @DeleteMapping("/{slug}/image")
  @Operation(
      summary = "Delete a photo for a user",
      description = "Delete the photo for the user identified by their ID",
      responses = {
        @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  @PreAuthorize("@expressionService.canAccessUserDataBySlug(#slug)")
  public void deleteUserPhoto(@PathVariable(name = "slug") String slug) {
    userService.deleteUserPhoto(slug);
  }
}
