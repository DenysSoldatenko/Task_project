package com.example.taskmanagerproject.dtos.tasks;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a comment on a task.
 */
@Schema(description = "DTO representing a task comment within the task discussion")
public record TaskCommentDto(

    @Schema(
      description = "Unique identifier of the task comment",
      hidden = true
    )
    Long id,

    @NotNull(message = "Task cannot be null")
    @Schema(
      description = "The task this comment belongs to",
      implementation = TaskDto.class
    )
    TaskDto task,

    @NotNull(message = "Sender cannot be null")
    @Schema(
      description = "The user who sent the comment",
      implementation = UserDto.class
    )
    UserDto sender,

    @NotNull(message = "Receiver cannot be null")
    @Schema(
      description = "The user who receives the comment",
      implementation = UserDto.class
    )
    UserDto receiver,

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "The unique identifier or slug for the comment",
      example = "task-1234-review"
    )
    String slug,

    @NotNull(message = "Message cannot be null")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    @Schema(
      description = "The actual message of the comment",
      example = "Please review the changes made to the task"
    )
    String message,

    @JsonProperty(access = READ_ONLY)
    @Schema(
      description = "Date and time when the comment was created",
      example = "2025-02-03T13:10:20"
    )
    LocalDateTime createdAt,

    @NotNull(message = "Resolved status cannot be null")
    @Schema(
      description = "Indicates whether the comment has been resolved",
      example = "false"
    )
    Boolean isResolved
) {}
