package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.ASSIGNED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.IN_PROGRESS;
import static java.lang.Math.min;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.repositories.TaskHistoryRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
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
  private static final int BATCH_SIZE = 500;
  private static final int MIN_APPROVED_TIME_DIFFERENCE = 10;

  private final TaskRepository taskRepository;
  private final TaskHistoryRepository taskHistoryRepository;
  private final KafkaTemplate<String, KafkaTaskCompletionDto> kafkaTemplate;

  /**
   * Changes the status of tasks to APPROVED for tasks with even IDs and not in approved, assigned,
   * or in-progress status.
   *
   * @return the number of tasks updated
   */
  public int changeTaskStatusForAllUsers() {
    List<Task> tasksToApprove = taskRepository.findAll().stream()
        .filter(task -> task.getId() % 10 < 7 && !EnumSet.of(APPROVED, ASSIGNED, IN_PROGRESS).contains(task.getTaskStatus()))
        .map(task -> {
          task.setTaskStatus(APPROVED);
          task.setApprovedAt(calculateApprovedAt(task));
          return task;
        })
        .toList();

    saveInBatches(tasksToApprove, taskRepository::saveAll);
    return tasksToApprove.size();
  }

  /**
   * Generates achievement events for users based on approved tasks.
   *
   * @return the number of tasks for which achievements were generated
   */
  public int generateAchievementsForUser() {
    List<Task> tasks = taskRepository.findRandomApprovedTasksForUserByTeamAndProject();
    tasks.stream()
      .map(this::toKafkaDto)
        .forEach(taskDto -> kafkaTemplate.send(ACHIEVEMENT_TOPIC, taskDto));
    return tasks.size();
  }

  /**
   * Updates task history with the approved timestamp for all tasks that were approved.
   *
   * @return the number of task histories updated
   */
  public int updateTaskHistoryUpdatedAtForAllUsers() {
    List<TaskHistory> updatedHistories = taskHistoryRepository.findAllByNewValue(APPROVED).stream()
        .map(history -> {
          history.setUpdatedAt(history.getTask().getApprovedAt());
          return history;
        })
        .toList();

    saveInBatches(updatedHistories, taskHistoryRepository::saveAll);
    return updatedHistories.size();
  }

  private KafkaTaskCompletionDto toKafkaDto(Task task) {
    return new KafkaTaskCompletionDto(
      task.getId(),
      task.getAssignedTo().getId(),
      task.getTeam().getId(),
      task.getProject().getId()
    );
  }

  private LocalDateTime calculateApprovedAt(Task task) {
    LocalDateTime approvedAt = task.getExpirationDate().plusDays((long) RANDOM.nextInt(MAX_EXPIRATION_DAYS) + MIN_EXPIRATION_DAYS);
    return approvedAt.isBefore(task.getCreatedAt().plusMinutes(MIN_APPROVED_TIME_DIFFERENCE))
      ? task.getCreatedAt().plusMinutes(MIN_APPROVED_TIME_DIFFERENCE)
      : approvedAt;
  }

  private <T> void saveInBatches(List<T> items, Consumer<List<T>> saveFunction) {
    for (int i = 0; i < items.size(); i += BATCH_SIZE) {
      saveFunction.accept(items.subList(i, min(i + BATCH_SIZE, items.size())));
    }
  }
}
