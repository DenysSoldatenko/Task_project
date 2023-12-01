package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.TaskStatus;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.mappers.TaskMapper;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.TaskService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the TaskService interface.
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;

  @Override
  @Transactional(readOnly = true)
  public TaskDto getTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
    return taskMapper.toDto(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> getAllTasksByUserId(Long userId) {
    List<Task> taskList = taskRepository.findAllTasksByUserId(userId);
    return taskList.stream().map(taskMapper::toDto).toList();
  }

  @Override
  @Transactional
  public TaskDto updateTask(TaskDto taskDto, Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

    task.setTitle(task.getTitle());
    task.setDescription(task.getDescription());
    task.setTaskStatus(
        taskDto.taskStatus() != null ? taskDto.taskStatus() : TaskStatus.IN_PROGRESS
    );
    task.setExpirationDate(task.getExpirationDate());

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDto(updatedTask);
  }

  @Override
  @Transactional
  public TaskDto createTaskForUser(TaskDto taskDto, Long userId) {
    Task task = taskMapper.toEntity(taskDto);

    task.setTaskStatus(
        taskDto.taskStatus() != null ? taskDto.taskStatus() : TaskStatus.IN_PROGRESS
    );

    Task createdTask = taskRepository.save(task);
    taskRepository.assignTaskToUser(userId, task.getId());
    return taskMapper.toDto(createdTask);
  }

  @Override
  @Transactional
  public void deleteTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
    taskRepository.delete(task);
  }
}