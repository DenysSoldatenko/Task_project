package com.example.taskmanagerproject.services.impl;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.LongStream.rangeClosed;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.TaskMetricsService;
import java.time.LocalDateTime;
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

  @Override
  public long countApprovedTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).size();
  }

  @Override
  public boolean hasApprovedTasksInLast30Days(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> task.getApprovedAt().isAfter(now().minusDays(30)))
      .count() >= 30;
  }

  @Override
  public boolean hasApprovedTasksBeforeDeadline(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> task.getExpirationDate().isAfter(task.getApprovedAt()))
      .count() >= 20;
  }

  @Override
  public boolean hasApprovedHighPriorityTasks(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "HIGH".equals(task.getPriority().name()))
      .count() >= 20;
  }

  @Override
  public boolean hasApprovedCriticalPriorityTasks(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .count() >= 40;
  }

  @Override
  public boolean hasApprovedTasksDaily(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .collect(groupingBy(task -> task.getApprovedAt().toLocalDate(), counting()))
      .values().stream()
      .anyMatch(count -> count >= 5);
  }

  @Override
  public boolean hasTasksApprovedAfterRejection(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskRepository.hasTaskBeenCancelled(task.getId()))
      .count() >= 10;
  }



  @Override
  public boolean hasFixedCriticalBugsInOneMonth(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .collect(groupingBy(task -> YearMonth.from(task.getApprovedAt()), counting()))
      .values().stream()
      .max(Long::compare)
      .orElse(0L) >= 20;
  }

  @Override
  public boolean hasFixedBugs(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 100;
  }

  @Override
  public boolean hasReportedBugs(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()) && taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 25;
  }

  @Override
  public boolean hasResolvedReviewComments(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count() >= 30;
  }



  @Override
  public boolean hasApprovedTasks10PercentFaster(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .mapToLong(task -> between(task.getCreatedAt(), task.getApprovedAt()).toMillis())
      .average()
      .stream()
      .anyMatch(avg -> getUserCompletedTasks(event).stream()
        .filter(task -> between(task.getCreatedAt(), task.getApprovedAt()).toMillis() <= avg * 0.9)
        .count() >= 20);
  }

  @Override
  public boolean hasMaintained90PercentOnTimeApprovalRate(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .mapToLong(task -> !task.getApprovedAt().isAfter(task.getExpirationDate()) ? 1 : 0)
      .sum() * 100 / getUserCompletedTasks(event).size() >= 90;
  }

  @Override
  public boolean hasApprovedCriticalTaskWithin24Hours(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .anyMatch(task -> "CRITICAL".equals(task.getPriority().name())
        && between(task.getCreatedAt(), task.getApprovedAt()).toHours() <= 24);
  }

  @Override
  public boolean hasSavedProjectByApprovingTaskJustBeforeDeadline(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .anyMatch(task -> between(task.getExpirationDate(), task.getApprovedAt()).toMinutes() <= 5);
  }



  @Override
  public boolean hasCollaboratedWithMultipleTeams(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .map(task -> task.getTeam().getId())
      .distinct()
      .count() >= 5;
  }

  @Override
  public boolean hasWorkedContinuouslyFor6Months(KafkaTaskCompletionDto event) {
    var tasks = getUserCompletedTasks(event);
    var earliestTask = tasks.stream()
        .map(Task::getCreatedAt)
        .min(LocalDateTime::compareTo)
        .orElse(null);

    if (earliestTask == null || earliestTask.isAfter(now().minusMonths(6))) {
      return false;
    }

    return rangeClosed(0, 5)
      .allMatch(
        month -> tasks.stream()
        .anyMatch(
          task -> task.getCreatedAt().isAfter(now().minusMonths(month + 1))
            && task.getCreatedAt().isBefore(now().minusMonths(month))
        )
      );
  }


  @Override
  public boolean hasCompletedLongDurationTasks(KafkaTaskCompletionDto event) {
    return getUserCompletedTasks(event).stream()
      .mapToLong(task -> between(task.getCreatedAt(), task.getApprovedAt()).toDays())
      .filter(days -> days > 7)
      .count() >= 50;
  }

  @Override
  public boolean hasMaintained90PercentCompletionFor12Months(KafkaTaskCompletionDto event) {
    var tasks = getUserCompletedTasks(event);

    if (tasks.isEmpty()) {
      return false;
    }

    long totalTasks = tasks.size();
    long recentTasks = tasks.stream().filter(task -> task.getCreatedAt().isAfter(now().minusMonths(12))).count();

    boolean hasTaskEachMonth = rangeClosed(0, 11)
        .allMatch(
          month -> tasks.stream()
          .anyMatch(
            task -> task.getCreatedAt().isAfter(now().minusMonths(month + 1))
              && task.getCreatedAt().isBefore(now().minusMonths(month))
          )
        );

    return hasTaskEachMonth && (recentTasks * 100 / totalTasks >= 90);
  }

  private List<Task> getUserCompletedTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId());
  }
}
