package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.utils.MessageUtil.NO_IMAGE_TO_DELETE;
import static com.example.taskmanagerproject.utils.MessageUtil.NO_IMAGE_TO_UPDATE;
import static com.example.taskmanagerproject.utils.MessageUtil.TASK_NOT_FOUND_WITH_ID;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.exceptions.ImageProcessingException;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import com.example.taskmanagerproject.utils.factories.TaskFactory;
import com.example.taskmanagerproject.utils.mappers.TaskMapper;
import com.example.taskmanagerproject.utils.validators.TaskValidator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final UserService userService;
  private final TaskFactory taskFactory;
  private final ImageService imageService;
  private final TaskValidator taskValidator;
  private final TaskRepository taskRepository;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "TaskService::getById", key = "#taskId")
  public TaskDto getTaskById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));
    return taskMapper.toDto(task);
  }

  @Override
  @Transactional
  @CachePut(value = "TaskService::getById", key = "#taskId")
  public TaskDto updateTask(TaskDto taskDto, Long taskId) {
    taskValidator.validateTaskDto(taskDto);
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));

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
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));
    taskRepository.delete(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskDto> findAllSoonExpiringTasks(String username, Duration duration, String projectName, String teamName) {
    LocalDateTime now = LocalDateTime.now();
    Long userId = userService.getUserByUsername(username).getId();
    return taskRepository.findExpiringTasksForUser(now, now.plus(duration), projectName, teamName, userId).stream()
      .map(taskMapper::toDto)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TaskDto> getAllTasksAssignedToUser(String slug, String projectName, String teamName, Pageable pageable) {
    Page<Task> tasksPage = taskRepository.findTasksAssignedToUser(slug, projectName, teamName, pageable);
    return tasksPage.map(taskMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TaskDto> getAllTasksAssignedByUser(String slug, String projectName, String teamName, Pageable pageable) {
    Page<Task> tasksPage = taskRepository.findTasksAssignedByUser(slug, projectName, teamName, pageable);
    return tasksPage.map(taskMapper::toDto);
  }

  @Override
  @Transactional
  public void uploadImage(Long taskId, TaskImageDto image) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));

    String fileName = imageService.uploadTaskImage(image);
    task.getImages().add(fileName);
    taskRepository.save(task);
  }

  @Override
  @Transactional
  public void updateImage(Long id, TaskImageDto imageDto, String imageName) {
    Task task = taskRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + id));

    String oldImage = task.getImages().stream()
        .filter(image -> image.equals(imageName))
        .findFirst()
        .orElseThrow(() -> new ImageProcessingException(NO_IMAGE_TO_UPDATE));

    String newImage = imageService.uploadTaskImage(imageDto);
    imageService.deleteImage(oldImage);

    task.getImages().remove(oldImage);
    task.getImages().add(newImage);
    taskRepository.save(task);
  }

  @Override
  @Transactional
  public void deleteImage(Long id, String imageName) {
    Task task = taskRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_NOT_FOUND_WITH_ID + id));

    if (!task.getImages().remove(imageName)) {
      throw new ImageProcessingException(NO_IMAGE_TO_DELETE);
    }

    imageService.deleteImage(imageName);
    taskRepository.save(task);
  }
}
