package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.TaskStatus;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.mappers.TaskMapper;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.TaskService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
  private final ImageService imageService;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "TaskService::getById", key = "#taskId")
  public TaskDto getTaskById(final Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
    return taskMapper.toDto(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> getAllTasksByUserId(final Long userId) {
    List<Task> taskList = taskRepository.findAllTasksByUserId(userId);
    return taskList.stream().map(taskMapper::toDto).toList();
  }

  @Override
  @Transactional
  @CachePut(value = "TaskService::getById", key = "#taskId")
  public TaskDto updateTask(final TaskDto taskDto, final Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

    task.setTitle(taskDto.title());
    task.setDescription(taskDto.description());
    task.setTaskStatus(
        taskDto.taskStatus() != null ? taskDto.taskStatus()
          : TaskStatus.IN_PROGRESS
    );
    task.setExpirationDate(taskDto.expirationDate());
    task.setImages(taskDto.images());

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDto(updatedTask);
  }

  @Override
  @Transactional
  public TaskDto createTaskForUser(final TaskDto taskDto, final Long userId) {
    Task task = taskMapper.toEntity(taskDto);

    task.setTaskStatus(
        taskDto.taskStatus() != null ? taskDto.taskStatus()
          : TaskStatus.IN_PROGRESS
    );
    task.setImages(taskDto.images());

    Task createdTask = taskRepository.save(task);
    taskRepository.assignTaskToUser(userId, task.getId());
    return taskMapper.toDto(createdTask);
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void deleteTaskById(final Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
    taskRepository.delete(task);
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void uploadImage(final Long taskId, final TaskImageDto image) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

    String fileName = imageService.uploadImage(image);
    task.getImages().add(fileName);
    taskRepository.save(task);
  }
}
