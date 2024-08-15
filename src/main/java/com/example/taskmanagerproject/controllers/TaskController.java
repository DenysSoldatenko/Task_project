package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.dtos.task.TaskImageDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
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
      description = "Retrieve a task by its ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TaskDto.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @QueryMapping(name = "getTaskById")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public TaskDto getTaskById(
      @PathVariable(name = "id") @Argument Long id
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
      description = "Update an existing task by its ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Task updated successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TaskDto.class))
          ),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(OK)
  @MutationMapping(name = "updateTask")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public TaskDto updateTask(
      @Valid @RequestBody @Argument TaskDto taskDto,
      @PathVariable(name = "id") @Argument Long id
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
      description = "Delete a task by its ID",
      responses = {
          @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(NO_CONTENT)
  @MutationMapping(name = "deleteTaskById")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public void deleteTaskById(
      @PathVariable(name = "id") @Argument Long id
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
      description = "Upload an image for the task identified by its ID",
      responses = {
          @ApiResponse(responseCode = "204", description = "Image uploaded successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          ),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ErrorDetails.class))
          )
      }
  )
  @ResponseStatus(NO_CONTENT)
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  public void uploadImage(
      @Valid @ModelAttribute TaskImageDto imageDto,
      @PathVariable(name = "id") Long id
  ) {
    taskService.uploadImage(id, imageDto);
  }
}
