package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
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
   * Retrieves all comments associated with a specific task using the task's slug.
   *
   * @param slug The unique slug of the task whose comments are to be retrieved.
   * @return A list of TaskCommentDto objects corresponding to the specified task.
   */
  List<TaskCommentDto> getCommentsByTaskSlug(String slug);
}
