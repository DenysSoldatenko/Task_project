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
 * Controller class for handling user-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class UserController {

  private final UserService userService;
  private final TaskService taskService;

  /**
   * Retrieves user information by ID.
   *
   * @param id The ID of the user to retrieve.
   * @return ResponseEntity containing the user DTO.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Retrieve user information by ID",
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
  @QueryMapping(name = "getUserById")
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public UserDto getUserById(
      @PathVariable(name = "id") @Argument final Long id
  ) {
    return userService.getUserById(id);
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
  @PreAuthorize("@expressionService.canAccessUser(#id)")
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
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public TaskDto createTaskForUser(
      @PathVariable(name = "id") @Argument final Long id,
      @Valid @RequestBody @Argument final TaskDto taskDto
  ) {
    return taskService.createTaskForUser(taskDto, id);
  }

  /**
   * Updates user information by ID.
   *
   * @param userDto The DTO containing the updated user information.
   * @param id      The ID of the user to update.
   * @return ResponseEntity containing the updated user DTO.
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update user",
      description = "Update user information by ID",
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
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public UserDto updateUser(
      @Valid @RequestBody @Argument final UserDto userDto,
      @PathVariable(name = "id") @Argument final Long id
  ) {
    return userService.updateUser(userDto, id);
  }

  /**
   * Deletes a user by ID.
   *
   * @param id The ID of the user to delete.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete user by ID",
      description = "Delete user by ID",
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
  @MutationMapping(name = "deleteUserById")
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public void deleteUserById(
      @PathVariable(name = "id") @Argument final Long id
  ) {
    userService.deleteUserById(id);
  }
}
