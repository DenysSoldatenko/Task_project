package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.task.TaskHistory;
import com.example.taskmanagerproject.entities.task.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the TaskHistory entity.
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

  /**
   * Finds task histories for a specific user and task status.
   * Joins the Task entity to access user information.
   *
   * @param userId The ID of the user whose task histories are to be fetched.
   * @param status The task status to filter the task histories by.
   * @return A list of TaskHistory records for the given user ID and task status.
   */
  @Query("SELECT th FROM TaskHistory th JOIN th.task t WHERE t.assignedTo.id = :userId AND th.newValue = :status")
  List<TaskHistory> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
}
