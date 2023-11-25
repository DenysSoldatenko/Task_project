package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.TaskDto;

import java.util.List;

public interface TaskService {

  TaskDto getTaskById(Long taskId);

  List<TaskDto> getAllTasksByUserId(Long userId);

  TaskDto updateTask(TaskDto task);

  TaskDto createTask(TaskDto task, Long userId);

  void deleteTaskById(Long taskId);
}
