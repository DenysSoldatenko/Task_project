package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
  @PreAuthorize("@expressionService.hasRoleAdmin(#slug)")
  public UserDto getUserBySlug(
    @PathVariable(name = "slug") @Argument final String slug
  ) {
    return userService.getUserBySlug(slug);
  }

  /**
   * Retrieves tasks assigned to a user by ID.
   *
   * @param id The ID of the user to retrieve tasks for.
   * @return ResponseEntity containing a list of task DTOs.
   */
  @GetMapping("/{id}/tasks")
  @Operation(
    summary = "Get tasks by user ID",
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
  @QueryMapping(name = "getTasksByUserId")
  @PreAuthorize("@expressionService.hasRoleAdmin(#id)")
  public List<TaskDto> getTasksByUserId(
    @PathVariable(name = "id") @Argument final Long id
  ) {
    return taskService.getAllTasksByUserId(id);
  }

  /**
   * Creates a task assigned to a user by ID.
   *
   * @param id      The ID of the user to assign the task to.
   * @param taskDto The DTO containing the task information.
   * @return ResponseEntity containing the created task DTO.
   */
  @PostMapping("/{id}/tasks")
  @Operation(
    summary = "Create task for user",
    description = "Create a task assigned to a user by ID",
    responses = {
      @ApiResponse(responseCode = "201", description = "Task created successfully",
        content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = TaskDto.class))
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
  @ResponseStatus(CREATED)
  @MutationMapping(name = "createTaskForUser")
  @PreAuthorize("@expressionService.hasRoleAdmin(#id)")
  public TaskDto createTaskForUser(
    @PathVariable(name = "id") @Argument final Long id,
    @Valid @RequestBody @Argument final TaskDto taskDto
  ) {
    return taskService.createTaskForUser(taskDto, id);
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
  @PreAuthorize("@expressionService.hasRoleAdmin(#slug)")
  public UserDto updateUser(
    @Valid @RequestBody @Argument final UserDto userDto,
    @PathVariable(name = "slug") @Argument final String slug
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
  @PreAuthorize("@expressionService.hasRoleAdmin(#slug)")
  public void deleteUserBySlug(
    @PathVariable(name = "slug") @Argument final String slug
  ) {
    userService.deleteUserBySlug(slug);
  }
}
