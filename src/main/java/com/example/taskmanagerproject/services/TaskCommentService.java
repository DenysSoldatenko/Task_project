package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * Retrieves task comments by slug with pagination.
   *
   * @param slug The slug of the task comments to retrieve.
   * @param pageable The pagination information (page number, size, and sort).
   * @return A paginated list of TaskCommentDto objects.
   */
  Page<TaskCommentDto> getCommentsByTaskSlug(String slug, Pageable pageable);

  /**
   * Retrieves the task ID associated with the given task's slug.
   *
   * @param slug The unique slug of the task.
   * @return The task ID corresponding to the given slug.
   */
  Long getTaskIdBySlug(String slug);

  /**
   * Retrieves the task ID associated with the given task comment ID.
   *
   * @param taskCommentId The ID of the task comment.
   * @return The task ID associated with the given task comment ID.
   */
  Long getTaskIdByTaskCommentId(Long taskCommentId);
}
