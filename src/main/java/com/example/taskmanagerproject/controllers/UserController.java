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
public class UserController {

  private final UserService userService;
  private final TaskService taskService;

  @PutMapping("/{id}")
  @Operation(
      summary = "Update user",
      description = "Update user information by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto,
                                            @PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(userService.updateUser(userDto, id), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Retrieve user information by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<UserDto> getUserById(@PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete user by ID",
      description = "Delete user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<Void> deleteUserById(@PathVariable(name = "id") Long id) {
    userService.deleteUserById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{id}/tasks")
  @Operation(
      summary = "Get tasks by user ID",
      description = "Retrieve tasks assigned to a user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<List<TaskDto>> getTasksByUserId(@PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(taskService.getAllTasksByUserId(id), HttpStatus.OK);
  }

  @PostMapping("/{id}/tasks")
  @Operation(
      summary = "Create task for user",
      description = "Create a task assigned to a user by ID"
  )
  @PreAuthorize("@expressionService.canAccessUser(#id)")
  public ResponseEntity<TaskDto> createTaskForUser(@PathVariable(name = "id") Long id,
                                                   @Valid @RequestBody TaskDto taskDto) {
    return new ResponseEntity<>(taskService.createTaskForUser(taskDto, id), HttpStatus.CREATED);
  }
}