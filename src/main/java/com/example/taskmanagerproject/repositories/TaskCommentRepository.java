package com.example.taskmanagerproject.repositories;

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
   * Retrieves all task comments associated with the given task ID.
   *
   * @param taskId The ID of the task for which comments are being retrieved.
   * @return A list of TaskComment entities associated with the specified task ID.
   */
  List<TaskComment> findByTaskId(Long taskId);
}
