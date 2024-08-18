package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.TaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling task-comment-related endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/task-comments")
@Tag(name = "Task Comment Controller", description = "Endpoints for managing task comments")
public class TaskCommentController {

  private final TaskCommentService taskCommentService;

  /**
   * Creates a new comment on a task.
   *
   * @param taskCommentDto The TaskCommentDto object containing the necessary data to create the comment.
   * @return The created TaskCommentDto object.
   */
  @PostMapping()
  @Operation(
      summary = "Create a new comment on a task",
      description = "Allows the creation of a new comment on a specific task",
      responses = {
          @ApiResponse(responseCode = "201", description = "Task comment created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(CREATED)
  public TaskCommentDto createTaskComment(@Valid @RequestBody TaskCommentDto taskCommentDto) {
    return taskCommentService.createComment(taskCommentDto);
  }

  /**
   * Retrieves a task comment by its ID.
   *
   * @param id The ID of the task comment to retrieve.
   * @return The TaskCommentDto object corresponding to the given ID.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get a task comment by ID",
      description = "Retrieve a task comment by its ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Task comment retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "404", description = "Task comment not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(OK)
  public TaskCommentDto getTaskCommentById(@PathVariable Long id) {
    return taskCommentService.getTaskCommentById(id);
  }

  /**
   * Updates an existing task comment.
   *
   * @param taskCommentDto The updated TaskCommentDto object.
   * @param id The ID of the task comment to update.
   * @return The updated TaskCommentDto object.
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update an existing task comment",
      description = "Update a task comment by its ID",
      responses = {
          @ApiResponse(responseCode = "200", description = "Task comment updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "404", description = "Task comment not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(OK)
  public TaskCommentDto updateTaskComment(@Valid @RequestBody TaskCommentDto taskCommentDto, @PathVariable Long id) {
    return taskCommentService.updateTaskComment(taskCommentDto, id);
  }

  /**
   * Deletes a task comment by its ID.
   *
   * @param id The ID of the task comment to delete.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a task comment by ID",
      description = "Delete a task comment by its ID",
      responses = {
          @ApiResponse(responseCode = "204", description = "Task comment deleted successfully"),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "404", description = "Task comment not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  public void deleteTaskComment(@PathVariable Long id) {
    taskCommentService.deleteTaskComment(id);
  }
}
