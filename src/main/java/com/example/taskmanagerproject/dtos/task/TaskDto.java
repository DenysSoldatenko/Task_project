package com.example.taskmanagerproject.dtos.task;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.example.taskmanagerproject.entities.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
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

    @NotNull(message = "Title cannot be null!")
    @NotBlank(message = "Title cannot be blank!")
    @Size(
      min = MIN_TITLE_LENGTH,
      max = MAX_TITLE_LENGTH,
      message = "Title must be between 2 and 100 characters long!"
    )
    @Schema(
      description = "The title of the task",
      example = "Buy groceries",
      maxLength = MAX_TITLE_LENGTH
    )
    String title,

    @NotNull(message = "Description cannot be null!")
    @NotBlank(message = "Description cannot be blank!")
    @Size(
      min = MIN_DESCRIPTION_LENGTH,
      max = MAX_DESCRIPTION_LENGTH,
      message = "Description must be between 2 and 255 characters long!"
    )
    @Schema(
      description = "The description of the task",
      example = "Get milk, bread, and eggs",
      maxLength = MAX_DESCRIPTION_LENGTH
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
    LocalDateTime expirationDate,

    @Schema(
      description = "List of images associated with the entity",
      example = "image.png"
    )
    @JsonProperty(access = READ_ONLY)
    List<String> images
) implements Serializable {
  private static final int MIN_TITLE_LENGTH = 2;
  private static final int MAX_TITLE_LENGTH = 100;
  private static final int MIN_DESCRIPTION_LENGTH = 2;
  private static final int MAX_DESCRIPTION_LENGTH = 255;
}
