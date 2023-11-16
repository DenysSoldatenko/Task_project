package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.entities.Task;

import java.util.List;

public interface TaskService {

  Task getTaskById(Long taskId);

  List<Task> getAllTasksByUserId(Long userId);

  Task updateTask(Task task);

  Task createTask(Task task, Long userId);

  void deleteTaskById(Long taskId);
}
