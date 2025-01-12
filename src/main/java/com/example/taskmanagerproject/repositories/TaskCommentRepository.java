package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.users.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the TaskComment entity.
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

  /**
   * Retrieves task comments by task slug with pagination.
   *
   * @param slug The slug of the task comments to retrieve.
   * @param pageable The pagination information (page number, size, and sort).
   * @return A paginated list of TaskComment objects.
   */
  @Query("SELECT t FROM TaskComment t WHERE t.slug = :slug")
  Page<TaskComment> findByTaskSlug(@Param("slug") String slug, Pageable pageable);

  /**
   * Find comments by task and sender.
   *
   * @param task   The task to which the comment belongs.
   * @param sender The sender (user) of the comment.
   * @return A list of TaskComment entities associated with the task and sender.
   */
  List<TaskComment> findByTaskAndSender(Task task, User sender);

  /**
   * Checks if a TaskComment exists for the given task ID.
   *
   * @param taskId The ID of the task to check.
   * @return true if a comment exists for the given task, false otherwise.
   */
  boolean existsByTaskId(Long taskId);

  /**
   * Find the distinct task ID associated with the given slug.
   *
   * @param slug The slug of the task.
   * @return The distinct task ID associated with the given slug.
   */
  @Query("SELECT DISTINCT tc.task.id FROM TaskComment tc WHERE tc.slug = :slug")
  Long findDistinctTaskIdBySlug(@Param("slug") String slug);

  /**
   * Find the distinct task ID associated with the given task comment ID.
   *
   * @param taskCommentId The task comment ID.
   * @return The distinct task ID associated with the given task comment ID.
   */
  @Query("SELECT DISTINCT tc.task.id FROM TaskComment tc WHERE tc.id = :taskCommentId")
  Long findDistinctTaskIdById(@Param("taskCommentId") Long taskCommentId);
}
