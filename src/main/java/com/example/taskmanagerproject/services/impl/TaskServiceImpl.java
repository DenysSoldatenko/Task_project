package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.services.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
  @Override
  public Task getTaskById(Long taskId) {
    return null;
  }

  @Override
  public List<Task> getAllTasksByUserId(Long userId) {
    return null;
  }

  @Override
  public Task updateTask(Task task) {
    return null;
  }

  @Override
  public Task createTask(Task task, Long userId) {
    return null;
  }

  @Override
  public void deleteTaskById(Long taskId) {

  }
}
