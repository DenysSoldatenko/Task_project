package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.TaskStatus;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;

  @Override
  @Transactional(readOnly = true)
  public Task getById(Long id) {
    return taskRepository.findById(id)
      .orElseThrow(() -> new TaskNotFoundException("Task not found."));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Task> getAllByUserId(Long id) {
    return taskRepository.findAllByUserId(id);
  }

  @Override
  @Transactional
  public Task update(Task task) {
    if (task.getTaskStatus() == null) {
      task.setTaskStatus(TaskStatus.IN_PROGRESS);
    }
    taskRepository.update(task);
    return task;
  }

  @Override
  @Transactional
  public Task create(Task task, Long userId) {
    task.setTaskStatus(TaskStatus.IN_PROGRESS);
    taskRepository.create(task);
    taskRepository.assignToUserById(task.getId(), userId);
    return task;
  }

  @Override
  @Transactional
  public void delete(Long id) {
    taskRepository.delete(id);
  }

}