package com.example.taskmanagerproject.dtos;

import com.example.taskmanagerproject.entities.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a task DTO (Data Transfer Object) in the project.
 */
public record TaskDto(

    @Schema(description = "The unique identifier of the task", hidden = true)
    Long id,

    @NotNull(message = "Title cannot be null!")
    @NotBlank(message = "Title cannot be blank!")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    @Schema(
      description = "The title of the task",
      example = "Buy groceries",
      maxLength = 100
    )
    String title,

    @NotNull(message = "Description cannot be null!")
    @NotBlank(message = "Description cannot be blank!")
    @Size(min = 2, max = 255, message = "Description must be between 2 and 255 characters long!")
    @Schema(
      description = "The description of the task",
      example = "Get milk, bread, and eggs",
      maxLength = 255
    )
    String description,

    @NotNull(message = "Task status cannot be null!")
    @Schema(
      description = "The status of the task",
      example = "NOT_STARTED"
    )
    TaskStatus taskStatus,

    @NotNull(message = "Expiration date and time cannot be null!")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @Schema(
      description = "The expiration date and time of the task",
      example = "2024-01-19 13:00:00.000000"
    )
    LocalDateTime expirationDate
) implements Serializable {}
