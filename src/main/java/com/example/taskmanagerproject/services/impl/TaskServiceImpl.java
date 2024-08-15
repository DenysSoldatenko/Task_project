package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.task.TaskStatus.IN_PROGRESS;
import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND;
import static java.sql.Timestamp.valueOf;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.dtos.task.TaskImageDto;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.utils.mappers.TaskMapper;
import java.time.Duration;
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
  @CachePut(value = "TaskService::getById", key = "#taskId")
  public TaskDto updateTask(TaskDto taskDto, Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

    task.setTitle(taskDto.title());
    task.setDescription(taskDto.description());
    task.setTaskStatus(taskDto.taskStatus() != null ? taskDto.taskStatus().name() : IN_PROGRESS.name());
    task.setImages(taskDto.images() != null ? taskDto.images() : task.getImages());

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDto(updatedTask);
  }

  @Override
  @Transactional
  public TaskDto createTaskForUser(TaskDto taskDto, Long userId) {
    Task task = taskMapper.toEntity(taskDto);
    task.setTaskStatus(taskDto.taskStatus() != null ? taskDto.taskStatus().name() : IN_PROGRESS.name());
    task.setImages(taskDto.images());

    Task createdTask = taskRepository.save(task);
    taskRepository.assignTaskToUser(userId, task.getId());
    return taskMapper.toDto(createdTask);
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void deleteTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));
    taskRepository.delete(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> findAllSoonExpiringTasks(Duration duration) {
    List<Task> taskList = taskRepository.findAllSoonExpiringTasks(
        valueOf(now()), valueOf(now().plus(duration))
    );
    return taskList.stream().map(taskMapper::toDto).toList();
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void uploadImage(Long taskId, TaskImageDto image) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND));

    String fileName = imageService.uploadImage(image);
    task.getImages().add(fileName);
    taskRepository.save(task);
  }
}
