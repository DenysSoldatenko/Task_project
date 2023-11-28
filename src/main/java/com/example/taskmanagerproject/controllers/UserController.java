package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UserController {

  private final UserService userService;
  private final TaskService taskService;

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto,
                                            @PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(userService.updateUser(userDto, id), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUserById(@PathVariable(name = "id") Long id) {
    userService.deleteUserById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{id}/tasks")
  public ResponseEntity<List<TaskDto>> getTasksByUserId(@PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(taskService.getAllTasksByUserId(id), HttpStatus.OK);
  }

  @PostMapping("/{id}/tasks")
  public ResponseEntity<TaskDto> createTaskForUser(@PathVariable(name = "id") Long id,
                                                   @Valid @RequestBody TaskDto taskDto) {
    return new ResponseEntity<>(taskService.createTaskForUser(taskDto, id), HttpStatus.CREATED);
  }
}