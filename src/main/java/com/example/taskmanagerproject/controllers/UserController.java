package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.mappers.TaskMapper;
import com.example.taskmanagerproject.mappers.UserMapper;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final TaskService taskService;

  private final UserMapper userMapper;
  private final TaskMapper taskMapper;

  @PutMapping
  public UserDto update(@Valid @RequestBody UserDto dto) {
    User user = userMapper.toEntity(dto);
    User updatedUser = userService.updateUser(user);
    return userMapper.toDto(updatedUser);
  }

  @GetMapping("/{id}")
  public UserDto getById(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return userMapper.toDto(user);
  }

  @DeleteMapping("/{id}")
  public void deleteById(@PathVariable Long id) {
    userService.deleteUserById(id);
  }

  @GetMapping("/{id}/tasks")
  public List<TaskDto> getTasksByUserId(@PathVariable Long id) {
    List<Task> tasks = taskService.getAllTasksByUserId(id);
    return tasks.stream().map(taskMapper::toDto).toList();
  }

  @PostMapping("/{id}/tasks")
  public TaskDto createTask(@PathVariable Long id,
                            @Valid @RequestBody TaskDto dto) {
    Task task = taskMapper.toEntity(dto);
    Task createdTask = taskService.createTask(task, id);
    return taskMapper.toDto(createdTask);
  }
}