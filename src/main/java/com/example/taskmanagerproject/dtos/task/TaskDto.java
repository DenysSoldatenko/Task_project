package com.example.taskmanagerproject.dtos.task;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.example.taskmanagerproject.dtos.project.ProjectDto;
import com.example.taskmanagerproject.dtos.security.UserDto;
import com.example.taskmanagerproject.dtos.team.TeamDto;
import com.example.taskmanagerproject.entities.task.TaskPriority;
import com.example.taskmanagerproject.entities.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a task DTO (Data Transfer Object) in the project.
 */
@Schema(description = "Data Transfer Object representing a task")
public record TaskDto(

    @Schema(
      description = "The unique identifier of the task",
      hidden = true
    )
    Long id,

    @NotNull(message = "Project cannot be null!")
    @Schema(
      description = "The project associated with the task",
      implementation = ProjectDto.class
    )
    ProjectDto project,

    @NotNull(message = "Team cannot be null!")
    @Schema(
      description = "The team associated with the task",
      implementation = TeamDto.class
    )
    TeamDto team,

    @NotNull(message = "Title cannot be null!")
    @NotBlank(message = "Title cannot be blank!")
    @Size(max = 255, message = "Title cannot exceed 255 characters!")
    @Schema(
      description = "The title of the task",
      example = "Fix the bug in the login module"
    )
    String title,

    @Size(max = 2000, message = "Description cannot exceed 2000 characters!")
    @Schema(
      description = "The detailed description of the task",
      example = "Fix the bug that causes login to fail for users with special characters in their password"
    )
    String description,

    @NotNull(message = "Expiration date cannot be null!")
    @Schema(
      description = "The expiration date for the task",
      example = "2025-09-16 15:00:00"
    )
    LocalDateTime expirationDate,

    @NotNull(message = "Task status cannot be null!")
    @Schema(
      description = "The status of the task",
      implementation = TaskStatus.class
    )
    TaskStatus taskStatus,

    @NotNull(message = "Priority cannot be null!")
    @Schema(
      description = "The priority of the task",
      implementation = TaskPriority.class
    )
    TaskPriority priority,

    @NotNull(message = "Assigned user cannot be null!")
    @Schema(
      description = "The user assigned to the task",
      implementation = UserDto.class
    )
    UserDto assignedTo,

    @NotNull(message = "Assigned by user cannot be null!")
    @Schema(
      description = "The user who assigned the task",
      implementation = UserDto.class
    )
    UserDto assignedBy,

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "List of images associated with the entity",
      example = "[\"image1.png\", \"image2.png\"]"
    )
    List<String> images,

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The date and time when the task was created",
      example = "2025-01-31 15:00:00"
    )
    LocalDateTime createdAt
) {}
