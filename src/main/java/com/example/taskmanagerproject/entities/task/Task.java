package com.example.taskmanagerproject.entities.task;

import static com.example.taskmanagerproject.entities.task.TaskPriority.MEDIUM;
import static com.example.taskmanagerproject.entities.task.TaskStatus.ASSIGNED;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.team.Team;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a task assigned within a project.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private LocalDateTime expirationDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "task_status", nullable = false)
  private TaskStatus taskStatus = ASSIGNED;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private TaskPriority priority = MEDIUM;

  @ManyToOne
  @JoinColumn(name = "assigned_to")
  private User assignedTo;

  @ManyToOne
  @JoinColumn(name = "assigned_by")
  private User assignedBy;

  @ElementCollection
  @Column(name = "image")
  @CollectionTable(name = "tasks_images")
  private List<String> images;
}
