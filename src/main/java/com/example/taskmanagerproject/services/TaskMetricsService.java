package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;

/**
 * Service interface for handling task-related metrics calculations.
 */
public interface TaskMetricsService {

  /**
   * Counts the total number of completed tasks for a specific user, project, and team.
   *
   * @param event The Kafka event containing task completion details.
   * @return The number of completed tasks.
   */
  long countCompletedTasks(KafkaTaskCompletionDto event);

  /**
   * Checks whether the user has completed tasks in the last 30 days.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed tasks in the last 30 days, otherwise false.
   */
  boolean countTasksInLast30Days(KafkaTaskCompletionDto event);

  /**
   * Checks whether the user has completed tasks before their deadlines.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed tasks before their deadlines, otherwise false.
   */
  boolean countTasksBeforeDeadline(KafkaTaskCompletionDto event);

  /**
   * Counts the number of high-priority tasks completed by a user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed high-priority tasks, otherwise false.
   */
  boolean countHighPriorityTasks(KafkaTaskCompletionDto event);

  /**
   * Counts the number of critical-priority tasks completed by a user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed critical-priority tasks, otherwise false.
   */
  boolean countCriticalPriorityTasks(KafkaTaskCompletionDto event);

  /**
   * Checks whether the user has completed a certain number of tasks per day.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has completed tasks in a day, otherwise false.
   */
  boolean countTasksCompletedPerDay(KafkaTaskCompletionDto event);

  /**
   * Counts the number of tasks approved after rejection for a specific user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has tasks approved after rejection, otherwise false.
   */
  boolean countApprovedAfterRejection(KafkaTaskCompletionDto event);



  /**
   * Counts the number of fixed critical bugs completed by the user in one month.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has resolved critical bugs in the last month, otherwise false.
   */
  boolean countFixedCriticalBugsInOneMonth(KafkaTaskCompletionDto event);

  /**
   * Counts the number of bugs fixed by the user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has fixed bugs, otherwise false.
   */
  boolean countFixedBugs(KafkaTaskCompletionDto event);

  /**
   * Counts the number of bugs reported by the user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has reported bugs, otherwise false.
   */
  boolean countReportedBugs(KafkaTaskCompletionDto event);

  /**
   * Counts the number of review comments resolved by the user.
   *
   * @param event The Kafka event containing task completion details.
   * @return True if the user has resolved review comments, otherwise false.
   */
  boolean countResolvedReviewComments(KafkaTaskCompletionDto event);
}
