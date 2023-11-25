package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

  Optional<Task> getTaskById(Long taskId);

  List<Task> getAllTasksByUserId(Long userId);

  void assignTaskToUserById(@Param("taskId") Long taskId, @Param("userId") Long userId);

  Task updateTask(TaskDto task);

  Task createTask(TaskDto task);

  void deleteTaskById(Long taskId);
}
