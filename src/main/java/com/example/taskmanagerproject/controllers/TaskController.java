package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.mappers.TaskMapper;
import com.example.taskmanagerproject.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @PutMapping
  public TaskDto updateTask(@Valid @RequestBody TaskDto taskDto) {
    return null;
  }

  @GetMapping("/{id}")
  public TaskDto getTaskById(@PathVariable Long id) {
    return null;
  }

  @DeleteMapping("/{id}")
  public void deleteTaskById(@PathVariable Long id) {
    taskService.delete(id);
  }
}
