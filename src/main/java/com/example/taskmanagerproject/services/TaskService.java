package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import java.time.Duration;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * Retrieves a paginated list of tasks assigned to a specific user for a specific project and team.
   *
   * @param slug       The user's unique identifier (slug).
   * @param projectName The name of the project to filter tasks.
   * @param teamName   The name of the team to filter tasks.
   * @param pageable   The pagination details (page number, size, and sort order).
   * @return A paginated list of TaskDto objects representing tasks assigned to the user.
   */
  Page<TaskDto> getAllTasksAssignedToUser(String slug, String projectName, String teamName, Pageable pageable);

  /**
   * Retrieves a paginated list of tasks assigned by a specific user for a specific project and team.
   *
   * @param slug       The user's unique identifier (slug).
   * @param projectName The name of the project to filter tasks.
   * @param teamName   The name of the team to filter tasks.
   * @param pageable   The pagination details (page number, size, and sort order).
   * @return A paginated list of TaskDto objects representing tasks assigned by the user.
   */
  Page<TaskDto> getAllTasksAssignedByUser(String slug, String projectName, String teamName, Pageable pageable);

  /**
   * Uploads an image for a task.
   *
   * @param id The ID of the task to upload the image for.
   * @param image The TaskImageDto containing the image data to be uploaded.
   */
  void uploadImage(Long id, TaskImageDto image);

  /**
   * Updates an image for a task.
   *
   * @param id The ID of the task to update the image for.
   * @param imageDto The DTO containing the new image data to upload.
   * @param imageName The name of the existing image that is being replaced or updated.
   */
  void updateImage(Long id, TaskImageDto imageDto, String imageName);

  /**
   * Deletes an image from a task.
   *
   * @param id The ID of the task to delete the image from.
   * @param imageName The name of the image to delete from the task.
   */
  void deleteImage(Long id, String imageName);
}
