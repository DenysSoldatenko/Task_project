package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.dtos.task.TaskImageDto;
import java.time.Duration;
import java.util.List;

/**
 * Service interface for managing tasks.
 */
public interface TaskService {

  TaskDto getTaskById(Long taskId);

  List<TaskDto> getAllTasksByUserId(Long userId);

  TaskDto updateTask(TaskDto taskDto, Long taskId);

  TaskDto createTaskForUser(TaskDto taskDto, Long userId);

  void deleteTaskById(Long taskId);

  List<TaskDto> findAllSoonExpiringTasks(Duration duration);

  void uploadImage(Long id, TaskImageDto image);
}
