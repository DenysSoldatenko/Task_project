package com.example.taskmanagerproject.services.impl;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.TaskMetricsService;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TaskMetricsService interface.
 */
@Service
@RequiredArgsConstructor
public class TaskMetricsServiceImpl implements TaskMetricsService {

  private final TaskRepository taskRepository;
  private final TaskCommentRepository taskCommentRepository;

  // Task-Based Achievements
  @Override
  public long countCompletedTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).size();
  }

  @Override
  public boolean countTasksInLast30Days(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> task.getApprovedAt().isAfter(now().minusDays(30)))
      .count() >= 30;
  }

  @Override
  public boolean countTasksBeforeDeadline(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> task.getExpirationDate().isAfter(task.getApprovedAt()))
      .count() >= 20;
  }

  @Override
  public boolean countHighPriorityTasks(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "HIGH".equals(task.getPriority().name()))
      .count() >= 20;
  }

  @Override
  public boolean countCriticalPriorityTasks(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .count() >= 40;
  }

  @Override
  public boolean countTasksCompletedPerDay(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .collect(groupingBy(task -> task.getApprovedAt().toLocalDate(), counting()))
      .values().stream()
      .anyMatch(count -> count >= 5);
  }

  @Override
  public boolean countApprovedAfterRejection(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskRepository.hasTaskBeenCancelled(task.getId()))
      .count() >= 10;
  }



  // Bug Fixing & Issue Resolution
  @Override
  public boolean countFixedCriticalBugsInOneMonth(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .collect(groupingBy(task -> YearMonth.from(task.getApprovedAt()), counting()))
      .values().stream()
      .max(Long::compare)
      .orElse(0L) >= 20;
  }

  @Override
  public boolean countFixedBugs(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 100;
  }

  @Override
  public boolean countReportedBugs(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()) && taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 25;
  }

  @Override
  public boolean countResolvedReviewComments(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 30;
  }

  private List<Task> getUserCompletedTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId());
  }
}
