package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling user-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Endpoints for managing users")
public final class UserController {

  private final UserService userService;
  private final TaskService taskService;

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
      description = "Update user information by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<UserDto> updateUser(
      @Valid @RequestBody final UserDto userDto,
      @PathVariable(name = "id") final Long id
  ) {
    return new ResponseEntity<>(
      userService.updateUser(userDto, id), HttpStatus.OK
    );
  }

  /**
   * Retrieves user information by ID.
   *
   * @param id The ID of the user to retrieve.
   * @return ResponseEntity containing the user DTO.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Retrieve user information by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<UserDto> getUserById(
      @PathVariable(name = "id") final Long id
  ) {
    return new ResponseEntity<>(
      userService.getUserById(id), HttpStatus.OK
    );
  }

  /**
   * Deletes a user by ID.
   *
   * @param id The ID of the user to delete.
   * @return ResponseEntity indicating the success of the operation.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete user by ID",
      description = "Delete user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<Void> deleteUserById(
      @PathVariable(name = "id") final Long id
  ) {
    userService.deleteUserById(id);
    return new ResponseEntity<>(
      HttpStatus.NO_CONTENT
    );
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
      description = "Retrieve tasks assigned to a user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<List<TaskDto>> getTasksByUserId(
      @PathVariable(name = "id") final Long id
  ) {
    return new ResponseEntity<>(
      taskService.getAllTasksByUserId(id), HttpStatus.OK
    );
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
      description = "Create a task assigned to a user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<TaskDto> createTaskForUser(
      @PathVariable(name = "id") final Long id,
      @Valid @RequestBody final TaskDto taskDto
  ) {
    return new ResponseEntity<>(
      taskService.createTaskForUser(taskDto, id), HttpStatus.CREATED
    );
  }
}
