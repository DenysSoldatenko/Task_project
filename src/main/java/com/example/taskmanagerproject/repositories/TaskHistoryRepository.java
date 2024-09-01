package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.entities.tasks.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the TaskHistory entity.
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

  /**
   * Finds all task history entries by the specified new task status.
   *
   * @param taskStatus The new task status to filter task histories.
   * @return A list of TaskHistory objects that match the specified task status.
   */
  List<TaskHistory> findAllByNewValue(TaskStatus taskStatus);
}
