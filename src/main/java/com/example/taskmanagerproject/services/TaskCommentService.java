package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import java.util.List;

/**
 * Service interface for managing task comments.
 */
public interface TaskCommentService {

  /**
   * Creates a new comment for a specific task.
   *
   * @param taskCommentDto The DTO representing the comment to be created.
   * @return The created TaskCommentDto.
   */
  TaskCommentDto createComment(TaskCommentDto taskCommentDto);

  /**
   * Retrieves a task comment by its ID.
   *
   * @param id The ID of the task comment to retrieve.
   * @return The TaskCommentDto corresponding to the given ID.
   */
  TaskCommentDto getTaskCommentById(Long id);

  /**
   * Updates an existing task comment.
   *
   * @param taskCommentDto The DTO containing the updated comment data.
   * @param id The ID of the comment to update.
   * @return The updated TaskCommentDto.
   */
  TaskCommentDto updateTaskComment(TaskCommentDto taskCommentDto, Long id);

  /**
   * Deletes a task comment by its ID.
   *
   * @param id The ID of the task comment to delete.
   */
  void deleteTaskComment(Long id);

  /**
   * Retrieves all comments for a specific task.
   *
   * @param taskId The ID of the task for which to retrieve the comments.
   * @return A list of TaskCommentDto objects for the specified task.
   */
  List<TaskCommentDto> getCommentsByTaskId(Long taskId);
}
