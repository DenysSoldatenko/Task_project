package com.example.taskmanagerproject.entities.task;

import com.example.taskmanagerproject.entities.security.User;
import jakarta.persistence.Column;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tracks changes made to tasks, including status updates and comments.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_history")
public class TaskHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne
  @JoinColumn(name = "updated_by", nullable = false)
  private User updatedBy;

  @Enumerated(EnumType.STRING)
  private TaskStatus previousValue;

  @Enumerated(EnumType.STRING)
  private TaskStatus newValue;

  @Column(nullable = false, updatable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();
}
