package com.example.taskmanagerproject.dtos;

import com.example.taskmanagerproject.entities.TaskStatus;

import java.time.LocalDateTime;

/**
 * Represents a task DTO (Data Transfer Object) in the project.
 */
public record TaskDto(Long id, String title, String description,
                      TaskStatus taskStatus, LocalDateTime expirationDateTime) {
}
