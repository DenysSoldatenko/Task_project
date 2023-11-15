package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Task;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

  Optional<Task> getTaskById(Long taskId);

  List<Task> getAllTasksByUserId(Long userId);

  void assignTaskToUserById(@Param("taskId") Long taskId, @Param("userId") Long userId);

  void updateTask(Task task);

  void createTask(Task task);

  void deleteTaskById(Long taskId);
}
