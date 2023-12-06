package com.example.taskmanagerproject.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String description;

  @Enumerated(value = EnumType.STRING)
  private TaskStatus taskStatus;

  private LocalDateTime expirationDate;

  @Column(name = "image")
  @CollectionTable(name = "tasks_images")
  @ElementCollection
  private List<String> images;
}
