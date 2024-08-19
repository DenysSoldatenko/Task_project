package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.entities.task.TaskComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the TaskComment entity.
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

  /**
   * Retrieves a list of TaskComment entities by the given task slug.
   *
   * @param slug The unique identifier (slug) of the task for which comments are retrieved.
   * @return A list of TaskComment entities associated with the provided task slug.
   */
  List<TaskComment> findAllBySlug(String slug);

  /**
   * Find comments by task and sender.
   *
   * @param task   The task to which the comment belongs.
   * @param sender The sender (user) of the comment.
   * @return A list of TaskComment entities associated with the task and sender.
   */
  List<TaskComment> findByTaskAndSender(Task task, User sender);
}
