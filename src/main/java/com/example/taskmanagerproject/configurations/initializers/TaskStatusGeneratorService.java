package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.ASSIGNED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.IN_PROGRESS;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.repositories.TaskHistoryRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for handling task status updates and generates achievement events for users.
 */
@Service
@RequiredArgsConstructor
public class TaskStatusGeneratorService {

  private static final String ACHIEVEMENT_TOPIC = "achievement-topic";

  private static final Random RANDOM = new Random();
  private static final int MIN_EXPIRATION_DAYS = -3;
  private static final int MAX_EXPIRATION_DAYS = 3;

  private final TaskRepository taskRepository;
  private final TaskHistoryRepository taskHistoryRepository;
  private final KafkaTemplate<String, KafkaTaskCompletionDto> kafkaTemplate;

  /**
   * Changes the status of tasks assigned to all users, setting those with IDs divisible by 2 to APPROVED.
   * Skips tasks that are already marked as APPROVED, ASSIGNED, or IN_PROGRESS.
   *
   * @return the number of tasks whose status was updated to APPROVED
   */
  public int changeTaskStatusForAllUsers() {
    List<Task> tasksToApprove = taskRepository.findAll().stream()
        .filter(task -> task.getId() % 2 == 0 && !EnumSet.of(APPROVED, ASSIGNED, IN_PROGRESS).contains(task.getTaskStatus()))
        .peek(task -> {
          task.setTaskStatus(APPROVED);
          task.setApprovedAt(updateApprovedAt(task));
        })
        .toList();

    taskRepository.saveAll(tasksToApprove);
    return tasksToApprove.size();
  }

  /**
   * Generates achievement events for users by sending a Kafka message for each randomly approved task.
   * Each task's completion is recorded as an event, including task details, user, team, and project IDs.
   *
   * @return the number of achievement events sent
   */
  public int generateAchievementsForUser() {
    List<Task> tasks = taskRepository.findRandomApprovedTasksForUserByTeamAndProject();
    tasks.forEach(task -> kafkaTemplate.send(ACHIEVEMENT_TOPIC, createAchievementEvent(task)));
    return tasks.size();
  }

  private KafkaTaskCompletionDto createAchievementEvent(Task task) {
    return new KafkaTaskCompletionDto(task.getId(), task.getAssignedTo().getId(),
        task.getTeam().getId(), task.getProject().getId());
  }

  /**
   * Updates the "updatedAt" field for all task histories with the status "APPROVED".
   *
   * @return the number of task histories that were updated
   */
  public int updateTaskHistoryUpdatedAtForAllUsers() {
    List<TaskHistory> updatedHistories = taskHistoryRepository.findAll().stream()
        .filter(history -> history.getNewValue() == APPROVED)
        .peek(this::updateUpdatedAt)
        .toList();

    taskHistoryRepository.saveAll(updatedHistories);
    return updatedHistories.size();
  }

  private void updateUpdatedAt(TaskHistory taskHistory) {
    taskHistory.setUpdatedAt(taskHistory.getTask().getApprovedAt());
  }

  private LocalDateTime updateApprovedAt(Task task) {
    LocalDateTime approvedAt = task.getExpirationDate().plusDays(RANDOM.nextInt(MAX_EXPIRATION_DAYS) + MIN_EXPIRATION_DAYS);
    return approvedAt.isBefore(task.getCreatedAt().plusMinutes(10)) ? task.getCreatedAt().plusMinutes(10) : approvedAt;
  }
}
