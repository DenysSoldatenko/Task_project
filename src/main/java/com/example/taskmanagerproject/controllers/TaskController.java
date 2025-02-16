package com.example.taskmanagerproject.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling task-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/tasks")
@Tag(name = "Task Controller", description = "Endpoints for managing tasks")
public class TaskController {

  private final TaskService taskService;

  /**
   * Creates a new task for the specified user.
   *
   * @param taskDto The TaskDto object containing the necessary data for creating the task.
   *
   * @return A TaskDto object representing the created task.
   */
  @PostMapping()
  @Operation(
      summary = "Create a new task for a user",
      description = "Allows the creation of a new task",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Details of the task to be created", required = true,
        content = @Content(schema = @Schema(implementation = TaskDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
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
  public TaskDto createTask(
      @Valid @RequestBody @Argument TaskDto taskDto
  ) {
    return taskService.createTaskForUser(taskDto);
  }

  /**
   * Retrieves a task by its ID.
   *
   * @param id The ID of the task to retrieve.
   * @return ResponseEntity containing the task DTO.
   */
  @GetMapping("/{id}")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Get a task by ID",
      description = "Retrieve a task by its ID",
      parameters = {
        @Parameter(name = "id", description = "ID of the task to retrieve",
          required = true, in = ParameterIn.PATH, example = "1"),
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
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
  @ResponseStatus(OK)
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
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Update an existing task",
      description = "Update an existing task by its ID",
      parameters = {
        @Parameter(name = "id", description = "ID of the task to update",
          required = true, in = ParameterIn.PATH, example = "1"),
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Updated details of the task", required = true,
        content = @Content(schema = @Schema(implementation = TaskDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
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
  @ResponseStatus(OK)
  public TaskDto updateTask(
      @Valid @RequestBody @Argument TaskDto taskDto,
      @PathVariable(name = "id") @Argument Long id
  ) {
    return taskService.updateTask(taskDto, id);
  }

  /**
   * Retrieves all tasks that are set to expire within the specified duration from now,
   * filtered by project and team name.
   *
   * @param username    the username of the user
   * @param duration    the time window during which tasks are considered soon to expire
   * @param projectName the name of the project to filter tasks by
   * @param teamName    the name of the team to filter tasks by
   * @return a list of TaskDto objects representing tasks expiring soon
   */
  @GetMapping("/expiring-soon")
  @PreAuthorize("@expressionService.canAccessExpiringTasks(#username, #projectName, #teamName)")
  @Operation(
      summary = "Get tasks expiring soon",
      description = "Retrieves tasks that will expire within the given duration, filtered by project and team",
      parameters = {
        @Parameter(name = "username", description = "Username to filter tasks for",
          required = true, in = ParameterIn.QUERY, example = "alice12345@gmail.com"),
        @Parameter(name = "duration", description = "Duration window for tasks expiring soon",
          required = true, in = ParameterIn.QUERY, example = "PT3H"),
        @Parameter(name = "projectName", description = "Project name to filter tasks",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "teamName", description = "Team name to filter tasks",
          required = true, in = ParameterIn.QUERY, example = "Team Alpha"),
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(OK)
  public List<TaskDto> findAllSoonExpiringTasks(
      @RequestParam(name = "username") @Argument String username,
      @RequestParam(name = "duration") @Argument Duration duration,
      @RequestParam(name = "projectName") @Argument String projectName,
      @RequestParam(name = "teamName") @Argument String teamName
  ) {
    return taskService.findAllSoonExpiringTasks(username, duration, projectName, teamName);
  }

  /**
   * Deletes a task by its ID.
   *
   * @param id The ID of the task to delete.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Delete a task by ID",
      description = "Delete a task by its ID",
      parameters = {
        @Parameter(name = "id", description = "The ID of the task to delete",
          required = true, in = ParameterIn.PATH, example = "1")
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
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
  @ResponseStatus(NO_CONTENT)
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
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Upload an image for a task",
      description = "Upload an image for the task identified by its ID",
      parameters = {
        @Parameter(name = "id", description = "ID of the task to upload the image for",
          required = true, in = ParameterIn.PATH, example = "1"),
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Image details to upload for the task", required = true,
        content = @Content(schema = @Schema(implementation = TaskImageDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "204", description = "Image uploaded successfully"),
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
  public void uploadImage(
      @Valid @RequestBody @ModelAttribute TaskImageDto imageDto,
      @PathVariable(name = "id") Long id
  ) {
    taskService.uploadImage(id, imageDto);
  }

  /**
   * Updates an image for a task.
   *
   * @param imageDto The DTO containing the updated image data.
   * @param id       The ID of the task to update the image for.
   */
  @PutMapping("/{id}/image/{imageName}")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Update an image for a task",
      description = "Update the image for the task identified by its ID",
      parameters = {
        @Parameter(name = "id", description = "ID of the task to update the image for",
          required = true, in = ParameterIn.PATH, example = "1"),
        @Parameter(name = "imageName", description = "Name of the image to update",
          required = true, in = ParameterIn.PATH, example = "name.png")
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Updated image details for the task", required = true,
        content = @Content(schema = @Schema(implementation = TaskImageDto.class))
      ),
      responses = {
        @ApiResponse(responseCode = "200", description = "Image updated successfully"),
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
  public void updateImage(
      @Valid @RequestBody @ModelAttribute TaskImageDto imageDto,
      @PathVariable(name = "id") Long id,
      @PathVariable String imageName
  ) {
    taskService.updateImage(id, imageDto, imageName);
  }

  /**
   * Deletes an image for a task.
   *
   * @param id        The ID of the task.
   * @param imageName The name of the image to delete.
   */
  @DeleteMapping("/{id}/image/{imageName}")
  @PreAuthorize("@expressionService.canAccessTask(#id)")
  @Operation(
      summary = "Delete an image for a task",
      description = "Deletes a specific image from the task identified by its ID",
      parameters = {
        @Parameter(name = "id", description = "ID of the task",
          required = true, in = ParameterIn.PATH, example = "1"),
        @Parameter(name = "imageName", description = "Name of the image to delete",
          required = true, in = ParameterIn.PATH, example = "name.png")
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Task or image not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  @ResponseStatus(NO_CONTENT)
  public void deleteImage(
      @PathVariable Long id,
      @PathVariable String imageName
  ) {
    taskService.deleteImage(id, imageName);
  }
}
