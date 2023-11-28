package com.example.taskmanagerproject.dtos;

import com.example.taskmanagerproject.entities.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Represents a task DTO (Data Transfer Object) in the project.
 */
public record TaskDto(
    Long id,

    @NotNull(message = "Title cannot be null!")
    @NotBlank(message = "Title cannot be blank!")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    String title,

    @NotNull(message = "Description cannot be null!")
    @NotBlank(message = "Description cannot be blank!")
    @Size(min = 2, max = 255, message = "Description must be between 2 and 255 characters long!")
    String description,

    @NotNull(message = "Task status cannot be null!")
    TaskStatus taskStatus,

    @NotNull(message = "Expiration date and time cannot be null!")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime expirationDate
) {}
