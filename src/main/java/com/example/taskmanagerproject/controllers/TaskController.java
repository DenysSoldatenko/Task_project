package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling task-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @PutMapping("/{id}")
  public ResponseEntity<TaskDto> updateTask(@Valid @RequestBody TaskDto taskDto,
                                            @PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(taskService.updateTask(taskDto, id), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TaskDto> getTaskById(@PathVariable(name = "id") Long id) {
    return new ResponseEntity<>(taskService.getTaskById(id), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteTaskById(@PathVariable(name = "id") Long id) {
    taskService.deleteTaskById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
