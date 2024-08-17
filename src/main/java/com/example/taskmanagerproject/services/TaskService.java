package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.dtos.task.TaskImageDto;
import java.time.Duration;
import java.util.List;

/**
 * Service interface for managing tasks.
 */
public interface TaskService {

  /**
   * Retrieves a task by its unique identifier.
   *
   * @param taskId The ID of the task to retrieve.
   * @return The TaskDto representing the task with the given ID.
   */
  TaskDto getTaskById(Long taskId);

  /**
   * Updates an existing task with new details.
   *
   * @param taskDto The updated task data.
   * @param taskId The ID of the task to update.
   * @return The updated TaskDto object.
   */
  TaskDto updateTask(TaskDto taskDto, Long taskId);

  /**
   * Creates a new task for a specific user.
   *
   * @param taskDto The task data to create the task.
   * @return The created TaskDto object.
   */
  TaskDto createTaskForUser(TaskDto taskDto);

  /**
   * Deletes a task by its unique identifier.
   *
   * @param taskId The ID of the task to delete.
   */
  void deleteTaskById(Long taskId);

  /**
   * Retrieves a list of tasks that are soon expiring within a given duration.
   *
   * @param duration The duration within which tasks are considered soon to expire.
   * @return A list of TaskDto objects representing tasks that will expire soon.
   */
  List<TaskDto> findAllSoonExpiringTasks(Duration duration);

  /**
   * Uploads an image for a task.
   *
   * @param id The ID of the task to upload the image for.
   * @param image The TaskImageDto containing the image data.
   */
  void uploadImage(Long id, TaskImageDto image);

  /**
   * Retrieves all tasks assigned to a specific user.
   *
   * @param userId The ID of the user to retrieve tasks for.
   * @return A list of TaskDto objects representing the tasks assigned to the user.
   */
  List<TaskDto> getAllTasksAssignedToUser(Long userId);

  /**
   * Retrieves all tasks assigned by a specific user.
   *
   * @param userId The ID of the user who assigned the tasks.
   * @return A list of TaskDto objects representing the tasks assigned by the user.
   */
  List<TaskDto> getAllTasksAssignedByUser(Long userId);
}
