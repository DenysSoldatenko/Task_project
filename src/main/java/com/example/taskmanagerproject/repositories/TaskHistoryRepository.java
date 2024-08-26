package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the TaskHistory entity.
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

}
