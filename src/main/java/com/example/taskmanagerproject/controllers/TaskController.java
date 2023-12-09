package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling task-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "Endpoints for managing tasks")
public class TaskController {

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
  @ResponseStatus(HttpStatus.OK)
  @QueryMapping(name = "getTaskById")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public TaskDto getTaskById(
      @PathVariable(name = "id") @Argument final Long id
  ) {
    return taskService.getTaskById(id);
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
  @ResponseStatus(HttpStatus.OK)
  @MutationMapping(name = "updateTask")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public TaskDto updateTask(
      @Valid @RequestBody @Argument final TaskDto taskDto,
      @PathVariable(name = "id") @Argument final Long id
  ) {
    return taskService.updateTask(taskDto, id);
  }

  /**
   * Deletes a task by its ID.
   *
   * @param id The ID of the task to delete.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a task by ID",
      description = "Delete a task by its ID"
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @MutationMapping(name = "deleteTaskById")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public void deleteTaskById(
      @PathVariable(name = "id") @Argument final Long id
  ) {
    taskService.deleteTaskById(id);
  }

  /**
   * Uploads an image for a task.
   *
   * @param imageDto The DTO containing the image to upload.
   * @param id       The ID of the task to upload the image for.
   */
  @PostMapping("/{id}/image")
  @Operation(
      summary = "Upload an image for a task",
      description = "Upload an image for the task identified by its ID"
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public void uploadImage(
      @Valid @ModelAttribute final TaskImageDto imageDto,
      @PathVariable(name = "id") final Long id
  ) {
    taskService.uploadImage(id, imageDto);
  }
}
