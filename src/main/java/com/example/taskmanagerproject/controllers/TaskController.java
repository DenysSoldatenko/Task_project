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

  private final TaskMapper taskMapper;

  @PutMapping
  public TaskDto updateTask(@Valid @RequestBody TaskDto taskDto) {
    Task task = taskMapper.toEntity(taskDto);
    Task updatedTask = taskService.updateTask(task);
    return taskMapper.toDto(updatedTask);
  }

  @GetMapping("/{id}")
  public TaskDto getTaskById(@PathVariable Long id) {
    Task task = taskService.getTaskById(id);
    return taskMapper.toDto(task);
  }

  @DeleteMapping("/{id}")
  public void deleteTaskById(@PathVariable Long id) {
    taskService.deleteTaskById(id);
  }
}
