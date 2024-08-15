package com.example.taskmanagerproject.entities.task;

import com.example.taskmanagerproject.entities.security.User;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "users_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTask {

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Id
  @ManyToOne
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne
  @JoinColumn(name = "assigned_by")
  private User assignedBy;

  private LocalDateTime assignedDate;

  private String timeSpent;
}
