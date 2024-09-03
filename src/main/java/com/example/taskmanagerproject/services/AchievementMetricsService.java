package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;

/**
 * Service interface for handling achievement-related metrics calculations.
 * This interface provides methods for checking a user's achievement-based metrics
 * like task completion rates, bug fixes, task approvals, and more.
 */
public interface AchievementMetricsService {

  /**
   * Counts the total number of approved tasks assigned to a user for a specific project and team.
   *
   * @param event The Kafka event containing task approval details.
   * @return The number of approved tasks for the user.
   */
  long countApprovedTasks(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least 30 tasks in the last 30 days.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least 30 tasks in the last 30 days, otherwise false.
   */
  boolean hasApprovedTasksInLast30Days(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least 20 tasks before their deadlines.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least 20 tasks before their deadlines, otherwise false.
   */
  boolean hasApprovedTasksBeforeDeadline(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least 20 high-priority tasks.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least 20 high-priority tasks, otherwise false.
   */
  boolean hasApprovedHighPriorityTasks(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least 40 critical-priority tasks.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least 40 critical-priority tasks, otherwise false.
   */
  boolean hasApprovedCriticalPriorityTasks(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least 5 tasks in a single day.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least 5 tasks on a single day, otherwise false.
   */
  boolean hasApprovedTasksDaily(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has had at least 10 tasks approved after being rejected.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has had at least 10 tasks approved after being rejected, otherwise false.
   */
  boolean hasTasksApprovedAfterRejection(KafkaTaskCompletionDto event);



  /**
   * Checks if the user has fixed at least 20 critical bugs in the last month.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has fixed at least 20 critical bugs in the last month, otherwise false.
   */
  boolean hasFixedCriticalBugsInOneMonth(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has fixed at least 100 bugs.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has fixed at least 100 bugs, otherwise false.
   */
  boolean hasFixedBugs(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has reported at least 25 critical bugs.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has reported at least 25 critical bugs, otherwise false.
   */
  boolean hasReportedBugs(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has resolved at least 30 review comments.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has resolved at least 30 review comments, otherwise false.
   */
  boolean hasResolvedReviewComments(KafkaTaskCompletionDto event);



  /**
   * Checks if the user has approved at least 20 tasks 10% faster than the average approval time.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user approved tasks 10% faster than average, otherwise false.
   */
  boolean hasApprovedTasks10PercentFaster(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has maintained a 90% on-time approval rate.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has a 90% or higher on-time approval rate, otherwise false.
   */
  boolean hasMaintained90PercentOnTimeApprovalRate(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has approved at least one critical task (task with a 24-hour deadline) within 24 hours.
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved at least one urgent task within 24 hours, otherwise false.
   */
  boolean hasApprovedCriticalTaskWithin24Hours(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has saved a project by approving a task just before the deadline (within 5 minutes).
   *
   * @param event The Kafka event containing task approval details.
   * @return True if the user has approved a task within 5 minutes of the deadline, otherwise false.
   */
  boolean hasSavedProjectByApprovingTaskJustBeforeDeadline(KafkaTaskCompletionDto event);



  /**
   * Checks if the user has collaborated with 5 or more teams on cross-functional projects.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has collaborated with 5 or more teams, otherwise false.
   */
  boolean hasCollaboratedWithMultipleTeams(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has worked continuously for over 6 months.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has worked continuously for over 6 months, otherwise false.
   */
  boolean hasWorkedContinuouslyFor6Months(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has completed 50 or more long-duration tasks (tasks that took more than 7 days).
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed 50+ long-duration tasks, otherwise false.
   */
  boolean hasCompletedLongDurationTasks(KafkaTaskCompletionDto event);

  /**
   * Checks if the user has achieved a 90% or higher task completion consistency over the last 12 months.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has maintained 90% task completion consistency over the last 12 months, otherwise false.
   */
  boolean hasMaintained90PercentCompletionFor12Months(KafkaTaskCompletionDto event);
}
