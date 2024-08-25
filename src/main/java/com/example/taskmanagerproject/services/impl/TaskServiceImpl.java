package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND_WITH_ID;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.utils.factories.TaskFactory;
import com.example.taskmanagerproject.utils.mappers.TaskMapper;
import com.example.taskmanagerproject.utils.validators.TaskValidator;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the TaskService interface.
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final KafkaTemplate<String, KafkaTaskCompletionDto> kafkaTemplate;
  private static final String ACHIEVEMENT_TOPIC = "achievement-topic";

  private final TaskMapper taskMapper;
  private final ImageService imageService;
  private final TaskFactory taskFactory;
  private final TaskValidator taskValidator;
  private final TaskRepository taskRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "TaskService::getById", key = "#taskId")
  public TaskDto getTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));
    return taskMapper.toDto(task);
  }

  @Override
  @Transactional
  @CachePut(value = "TaskService::getById", key = "#taskId")
  public TaskDto updateTask(TaskDto taskDto, Long taskId) {
    taskValidator.validateTaskDto(taskDto);
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));

    task.setTitle(taskDto.title());
    task.setDescription(taskDto.description());
    task.setTaskStatus(taskDto.taskStatus() != null ? taskDto.taskStatus() : task.getTaskStatus());
    task.setPriority(taskDto.priority() != null ? taskDto.priority() : task.getPriority());
    task.setExpirationDate(taskDto.expirationDate());
    task.setApprovedAt(taskDto.taskStatus() == APPROVED ? now() : null);

    if (task.getTaskStatus().equals(APPROVED)) {
      KafkaTaskCompletionDto event = new KafkaTaskCompletionDto(
          task.getId(),
          task.getAssignedTo().getId(),
          task.getTeam().getId(),
          task.getProject().getId()
      );
      kafkaTemplate.send(ACHIEVEMENT_TOPIC, event);
    }

    Task updatedTask = taskRepository.save(task);
    return taskMapper.toDto(updatedTask);
  }

  @Override
  @Transactional
  public TaskDto createTaskForUser(TaskDto taskDto) {
    taskValidator.validateTaskDto(taskDto);
    Task createdTask = taskFactory.createTaskFromDto(taskDto);
    taskRepository.save(createdTask);
    return taskMapper.toDto(createdTask);
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void deleteTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));
    taskRepository.delete(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> findAllSoonExpiringTasks(Duration duration) {
    // TODO
//    List<Task> taskList = taskRepository.findExpiringTasksBetween(valueOf(now()), valueOf(now().plus(duration)));
//    return taskList.stream().map(taskMapper::toDto).toList();
    return null;
  }

  @Override
  @Transactional
  @CacheEvict(value = "TaskService::getById", key = "#taskId")
  public void uploadImage(Long taskId, TaskImageDto image) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));

    String fileName = imageService.uploadImage(image);
    task.getImages().add(fileName);
    taskRepository.save(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> getAllTasksAssignedToUser(Long userId) {
    List<Task> taskList = taskRepository.findTasksAssignedTo(userId);
    return taskList.stream().map(taskMapper::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> getAllTasksAssignedByUser(Long userId) {
    List<Task> taskList = taskRepository.findTasksAssignedBy(userId);
    return taskList.stream().map(taskMapper::toDto).toList();
  }
}
