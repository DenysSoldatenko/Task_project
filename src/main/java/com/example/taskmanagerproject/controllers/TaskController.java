package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@Tag(name = "Task Controller", description = "Endpoints for managing tasks")
public final class TaskController {

  private final TaskService taskService;

  /**
   * Retrieves a task by its ID.
   *
   * @param id The ID of the task to retrieve.
   * @return ResponseEntity containing the task DTO.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get a task by ID",
      description = "Retrieve a task by its ID"
  )
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public ResponseEntity<TaskDto> getTaskById(
      @PathVariable(name = "id") final Long id
  ) {
    return new ResponseEntity<>(
      taskService.getTaskById(id), HttpStatus.OK
    );
  }

  /**
   * Updates an existing task.
   *
   * @param taskDto The updated task DTO.
   * @param id      The ID of the task to update.
   * @return ResponseEntity containing the updated task DTO.
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update an existing task",
      description = "Update an existing task by its ID"
  )
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public ResponseEntity<TaskDto> updateTask(
      @Valid @RequestBody final TaskDto taskDto,
      @PathVariable(name = "id") final Long id
  ) {
    return new ResponseEntity<>(
      taskService.updateTask(taskDto, id), HttpStatus.OK
    );
  }

  /**
   * Deletes a task by its ID.
   *
   * @param id The ID of the task to delete.
   * @return ResponseEntity indicating the success of the operation.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a task by ID",
      description = "Delete a task by its ID"
  )
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public ResponseEntity<String> deleteTaskById(
      @PathVariable(name = "id") final Long id
  ) {
    taskService.deleteTaskById(id);
    return new ResponseEntity<>(
      HttpStatus.NO_CONTENT
    );
  }

  /**
   * Uploads an image for a task.
   *
   * @param imageDto The DTO containing the image to upload.
   * @param id       The ID of the task to upload the image for.
   * @return ResponseEntity indicating the success of the operation.
   */
  @PostMapping("/{id}/image")
  @Operation(
      summary = "Upload an image for a task",
      description = "Upload an image for the task identified by its ID"
  )
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public ResponseEntity<String> uploadImage(
      @Valid @ModelAttribute final TaskImageDto imageDto,
      @PathVariable(name = "id") final Long id
  ) {
    taskService.uploadImage(id, imageDto);
    return new ResponseEntity<>(
      HttpStatus.NO_CONTENT
    );
  }
}
