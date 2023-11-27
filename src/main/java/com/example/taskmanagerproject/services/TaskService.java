package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {

  Task getById(Long id);

  List<Task> getAllByUserId(Long id);

  Task update(Task task);

  Task create(Task task, Long userId);

  void delete(Long id);
}
