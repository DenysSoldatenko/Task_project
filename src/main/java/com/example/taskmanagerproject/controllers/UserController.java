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

  @PutMapping
  public UserDto update(@Valid @RequestBody UserDto dto) {
    return null;
  }

  @GetMapping("/{id}")
  public UserDto getById(@PathVariable Long id) {
    return null;
  }

  @DeleteMapping("/{id}")
  public void deleteById(@PathVariable Long id) {
    System.out.println("null");
  }

  @GetMapping("/{id}/tasks")
  public List<TaskDto> getTasksByUserId(@PathVariable Long id) {
    return null;
  }

  @PostMapping("/{id}/tasks")
  public TaskDto createTask(@PathVariable Long id,
                            @Valid @RequestBody TaskDto dto) {
    return null;
  }
}