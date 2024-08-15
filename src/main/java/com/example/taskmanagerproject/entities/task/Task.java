package com.example.taskmanagerproject.entities.task;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.team.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne
  @JoinColumn(name = "parent_task_id")
  private Task parentTask;

  @ManyToOne
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  private String title;

  private String description;

  private String taskStatus;

  private String priority;

  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "task")
  private List<TaskDependency> taskDependencies;

  @Column(name = "image")
  @ElementCollection
  @CollectionTable(name = "tasks_images")
  private List<String> images;

  @OneToMany(mappedBy = "task")
  private List<TaskComment> taskComments;

  @OneToMany(mappedBy = "task")
  private List<TaskHistory> taskHistories;
}
