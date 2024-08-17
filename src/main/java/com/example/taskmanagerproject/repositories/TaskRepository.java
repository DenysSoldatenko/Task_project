package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.task.Task;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Task entities.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  /**
   * Retrieves all tasks assigned by a specific user.
   *
   * @param userId the ID of the user who assigned the tasks
   * @return a list of tasks assigned by the specified user
   */
  @Query(value = """
      SELECT * FROM tasks t
      WHERE t.assigned_by = :userId
      """, nativeQuery = true)
  List<Task> findAllTasksAssignedByUser(@Param("userId") Long userId);

  /**
   * Retrieves all tasks assigned to a specific user.
   *
   * @param userId the ID of the user who is assigned the tasks
   * @return a list of tasks assigned to the specified user
   */
  @Query(value = """
      SELECT * FROM tasks t
      WHERE t.assigned_to = :userId
      """, nativeQuery = true)
  List<Task> findAllTasksAssignedToUser(@Param("userId") Long userId);

  @Query(value = """
      SELECT * FROM tasks t
      WHERE t.expiration_date is not null
      AND t.expiration_date between :start and :end
      """, nativeQuery = true)
  List<Task> findAllSoonExpiringTasks(@Param("start") Timestamp start, @Param("end") Timestamp end);
}
