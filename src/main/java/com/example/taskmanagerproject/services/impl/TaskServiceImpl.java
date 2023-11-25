package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.mappers.TaskMapper;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;

  @Override
  @Transactional(readOnly = true)
  public TaskDto getTaskById(Long taskId) {
    Task task = taskRepository.getTaskById(taskId)
      .orElseThrow(() -> new TaskNotFoundException("Task not found!"));

    return taskMapper.toDto(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> getAllTasksByUserId(Long userId) {
    List<Task> taskList = taskRepository.getAllTasksByUserId(userId);
    return taskList.stream().map(taskMapper::toDto).toList();
  }

  @Override
  @Transactional
  public TaskDto updateTask(TaskDto dto) {
    Task task = taskRepository.getTaskById(dto.id())
      .orElseThrow(() -> new TaskNotFoundException("Task not found!"));

    Task updatedTask = taskRepository.updateTask(dto);

    return taskMapper.toDto(updatedTask);
  }

  @Override
  @Transactional
  public TaskDto createTask(TaskDto taskDto, Long userId) {
    Task task = taskRepository.createTask(taskDto);

    return taskMapper.toDto(task);
  }

  @Override
  @Transactional
  public void deleteTaskById(Long taskId) {
    taskRepository.getTaskById(taskId)
      .orElseThrow(() -> new TaskNotFoundException("Task not found!"));
    taskRepository.deleteTaskById(taskId);
  }
}
