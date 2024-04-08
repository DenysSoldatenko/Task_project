package com.example.taskmanagerproject.entities;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Represents a task entity in the project.
 */
@Data
@Entity
@Table(name = "tasks")
public class Task implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String title;

  private String description;

  @Enumerated(value = STRING)
  private TaskStatus taskStatus;

  private LocalDateTime expirationDate;

  @Column(name = "image")
  @ElementCollection
  @CollectionTable(name = "tasks_images")
  private List<String> images;
}
