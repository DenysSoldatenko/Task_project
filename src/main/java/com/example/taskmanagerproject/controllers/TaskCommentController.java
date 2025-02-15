package com.example.taskmanagerproject.controllers;

import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.fromString;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.TaskCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling task-comment-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/task-comments")
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
  @PreAuthorize("@expressionService.canAccessTask(#taskCommentDto.task().id())")
  @Operation(
      summary = "Create a new comment on a task",
      description = "Allows the creation of a new comment on a specific task",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Details of the task comment to be created", required = true,
        content = @Content(schema = @Schema(implementation = TaskCommentDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "201", description = "Task comment created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
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
  @MutationMapping(name = "createTaskComment")
  public TaskCommentDto createTaskComment(
      @Valid @RequestBody @Argument TaskCommentDto taskCommentDto
  ) {
    return taskCommentService.createComment(taskCommentDto);
  }

  /**
   * Updates an existing task comment.
   *
   * @param taskCommentDto The updated TaskCommentDto object.
   * @param id The ID of the task comment to update.
   * @return The updated TaskCommentDto object.
   */
  @PutMapping("/{id}")
  @PreAuthorize("@expressionService.canAccessTask(#taskCommentDto.task().id())")
  @Operation(
      summary = "Update an existing task comment",
      description = "Update a task comment by its ID",
      parameters = {
        @Parameter(name = "id", description = "The ID of the task comment to update",
          required = true, in = ParameterIn.PATH, example = "1"),
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Updated task comment details", required = true,
        content = @Content(schema = @Schema(implementation = TaskCommentDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "200", description = "Task comment updated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
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
  @MutationMapping(name = "updateTaskComment")
  public TaskCommentDto updateTaskComment(
      @Valid @RequestBody @Argument TaskCommentDto taskCommentDto,
      @PathVariable(name = "id") @Argument Long id
  ) {
    return taskCommentService.updateTaskComment(taskCommentDto, id);
  }

  /**
   * Deletes a task comment by its ID.
   *
   * @param id The ID of the task comment to delete.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@expressionService.canAccessTaskComment(#id)")
  @Operation(
      summary = "Delete a task comment by ID",
      description = "Delete a task comment by its ID",
      parameters = {
        @Parameter(name = "id", description = "The ID of the task comment to delete",
          required = true, in = ParameterIn.PATH, example = "1"),
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Task comment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Task comment not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  @MutationMapping(name = "deleteTaskComment")
  public void deleteTaskComment(
      @PathVariable(name = "id") @Argument Long id
  ) {
    taskCommentService.deleteTaskComment(id);
  }

  /**
   * Retrieves task comments by their slug with pagination.
   *
   * @param slug The slug of the task comments to retrieve.
   * @param page The page number (0-based).
   * @param size The number of task comments per page.
   * @param sort The sort criteria (for example, "id,asc").
   * @return A paginated list of TaskCommentDto objects corresponding to the given slug.
   */
  @GetMapping("/{slug}")
  @PreAuthorize("@expressionService.canAccessTaskComment(#slug)")
  @Operation(
      summary = "Get task comments by slug",
      description = "Retrieve task comments by their unique slug with pagination",
      parameters = {
        @Parameter(name = "slug", description = "The slug of the task whose comments are to be retrieved",
          required = true, in = ParameterIn.PATH, example = "task-28210"),
        @Parameter(name = "page", description = "Page number (0-based)",
          required = true, in = ParameterIn.QUERY, example = "0"),
        @Parameter(name = "size", description = "Number of task comments per page",
          required = true, in = ParameterIn.QUERY, example = "10"),
        @Parameter(name = "sort", description = "Sort criteria (e.g., 'id,asc')",
          required = true, in = ParameterIn.QUERY, example = "id,asc"),
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Task comments retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskCommentDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Task comments not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(OK)
  @QueryMapping(name = "getTaskCommentsBySlug")
  public Page<TaskCommentDto> getTaskCommentsBySlug(
      @PathVariable(name = "slug") @Argument String slug,
      @RequestParam(defaultValue = "0") @Argument int page,
      @RequestParam(defaultValue = "10") @Argument int size,
      @RequestParam(defaultValue = "id,asc") @Argument String sort
  ) {
    String[] sortParams = sort.split(",");
    Direction direction = fromString(sortParams[1]);
    Pageable pageable = of(page, size, by(direction, sortParams[0]));
    return taskCommentService.getCommentsByTaskSlug(slug, pageable);
  }
}
