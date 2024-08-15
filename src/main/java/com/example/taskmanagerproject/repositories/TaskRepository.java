package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.task.Task;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Task entities.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query(value = """
      SELECT * FROM tasks t
      JOIN users_tasks ut ON ut.task_id = t.id
      WHERE ut.user_id = :userId
      """, nativeQuery = true)
  List<Task> findAllTasksByUserId(@Param("userId") Long userId);

  @Query(value = """
      SELECT * FROM tasks t
      WHERE t.expiration_date is not null
      AND t.expiration_date between :start and :end
      """, nativeQuery = true)
  List<Task> findAllSoonExpiringTasks(@Param("start") Timestamp start,
                                      @Param("end") Timestamp end);

  //@Transactional
  @Modifying
  @Query(value = """
      INSERT INTO users_tasks (user_id, task_id)
      VALUES (:userId, :taskId)
      """, nativeQuery = true)
  void assignTaskToUser(@Param("userId") Long userId,
                        @Param("taskId") Long taskId);
}
