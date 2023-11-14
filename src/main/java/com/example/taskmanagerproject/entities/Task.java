package com.example.taskmanagerproject.entities;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a task entity in the project.
 */
@Data
public class Task {

  private Long id;
  private String title;
  private String description;
  private TaskStatus taskStatus;
  private LocalDateTime expirationDateTime;
}
