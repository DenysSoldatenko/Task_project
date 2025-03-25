package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.HIGH;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.utils.MessageUtil.NO_IMAGE_TO_DELETE;
import static com.example.taskmanagerproject.utils.MessageUtil.NO_IMAGE_TO_UPDATE;
import static com.example.taskmanagerproject.utils.MessageUtil.TASK_NOT_FOUND_WITH_ID;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskStatus;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ImageProcessingException;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.services.UserService;
import com.example.taskmanagerproject.utils.factories.TaskFactory;
import com.example.taskmanagerproject.utils.mappers.TaskMapper;
import com.example.taskmanagerproject.utils.validators.TaskValidator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

class TaskServiceImplTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskMapper taskMapper;

  @Mock
  private TaskFactory taskFactory;

  @Mock
  private TaskValidator taskValidator;

  @Mock
  private KafkaTemplate<String, KafkaTaskCompletionDto> kafkaTemplate;

  @Mock
  private UserService userService;

  @Mock
  private ImageService imageService;

  @InjectMocks
  private TaskServiceImpl taskService;

  private static final String ACHIEVEMENT_TOPIC = "achievement-topic";

  private Pageable pageable;

  private Task task;
  private TaskDto taskDto;
  private TaskImageDto taskImageDto;

  private final Long taskId = 1L;
  private final Long userId = 1L;

  private final String slug = "test-slug";
  private final String title = "Test Task";
  private final String teamName = "TestTeam";
  private final String username = "testuser";
  private final String projectName = "TestProject";
  private final String description = "Test Description";
  private final String imageName = "test-image.jpg";

  private final TaskStatus status = APPROVED;
  private final LocalDateTime expirationDate = now().plusDays(1);

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    task = mock(Task.class);
    taskDto = mock(TaskDto.class);
    taskImageDto = mock(TaskImageDto.class);

    User user = mock(User.class);
    pageable = PageRequest.of(0, 10);

    when(user.getId()).thenReturn(userId);
    when(taskMapper.toDto(task)).thenReturn(taskDto);
    when(taskDto.title()).thenReturn(title);
    when(taskDto.description()).thenReturn(description);
    when(taskDto.taskStatus()).thenReturn(status);
    when(taskDto.priority()).thenReturn(HIGH);
    when(taskDto.expirationDate()).thenReturn(expirationDate);
    when(task.getId()).thenReturn(taskId);
    when(task.getAssignedTo()).thenReturn(user);
    when(task.getTeam()).thenReturn(mock(com.example.taskmanagerproject.entities.teams.Team.class));
    when(task.getTeam().getId()).thenReturn(1L);
    when(task.getProject()).thenReturn(mock(com.example.taskmanagerproject.entities.projects.Project.class));
    when(task.getProject().getId()).thenReturn(1L);
    when(task.getTaskStatus()).thenReturn(status);
    when(task.getImages()).thenReturn(new ArrayList<>(List.of(imageName)));
  }

  @Test
  void getTaskById_shouldReturnTaskDtoWhenTaskExists() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    TaskDto result = taskService.getTaskById(taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskRepository).findById(taskId);
    verify(taskMapper).toDto(task);
    verifyNoMoreInteractions(taskRepository, taskMapper);
  }

  @Test
  void getTaskById_shouldThrowResourceNotFoundExceptionWhenTaskNotFound() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId));
    assertEquals(TASK_NOT_FOUND_WITH_ID + taskId, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getTaskById_shouldThrowResourceNotFoundExceptionWhenIdIsZero() {
    when(taskRepository.findById(0L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(0L));
    assertEquals(TASK_NOT_FOUND_WITH_ID + 0L, exception.getMessage());
    verify(taskRepository).findById(0L);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getTaskById_shouldHandleNegativeId() {
    when(taskRepository.findById(-1L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(-1L));
    assertEquals(TASK_NOT_FOUND_WITH_ID + -1L, exception.getMessage());
    verify(taskRepository).findById(-1L);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void updateTask_shouldUpdateAndReturnTaskDtoWhenApproved() {
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.updateTask(taskDto, taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle(title);
    verify(task).setDescription(description);
    verify(task).setTaskStatus(status);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(any(LocalDateTime.class));
    verify(kafkaTemplate).send(eq(ACHIEVEMENT_TOPIC), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
  }

  @Test
  void updateTask_shouldUpdateWithoutKafkaWhenNotApproved() {
    when(taskDto.taskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
    when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.updateTask(taskDto, taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle(title);
    verify(task).setDescription(description);
    verify(task).setTaskStatus(TaskStatus.IN_PROGRESS);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(null);
    verify(kafkaTemplate, never()).send(anyString(), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
  }

  @Test
  void updateTask_shouldRetainExistingStatusWhenDtoStatusNull() {
    when(taskDto.taskStatus()).thenReturn(null);
    when(task.getTaskStatus()).thenReturn(TaskStatus.IN_PROGRESS);
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.updateTask(taskDto, taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle(title);
    verify(task).setDescription(description);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(null);
    verify(kafkaTemplate, never()).send(anyString(), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
  }

  @Test
  void updateTask_shouldRetainExistingPriorityWhenDtoPriorityNull() {
    when(taskDto.priority()).thenReturn(null);
    when(task.getPriority()).thenReturn(HIGH);
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.updateTask(taskDto, taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle(title);
    verify(task).setDescription(description);
    verify(task).setTaskStatus(status);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(any(LocalDateTime.class));
    verify(kafkaTemplate).send(eq(ACHIEVEMENT_TOPIC), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
  }

  @Test
  void updateTask_shouldThrowResourceNotFoundExceptionWhenTaskNotFound() {
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(taskDto, taskId));
    assertEquals(TASK_NOT_FOUND_WITH_ID + taskId, exception.getMessage());
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void updateTask_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    doThrow(new IllegalArgumentException("Invalid task title")).when(taskValidator).validateTaskDto(taskDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskDto, taskId));
    assertEquals("Invalid task title", exception.getMessage());
    verify(taskValidator).validateTaskDto(taskDto);
    verifyNoInteractions(taskRepository, kafkaTemplate);
  }

  @Test
  void updateTask_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskRepository).save(task);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskService.updateTask(taskDto, taskId));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle(title);
    verify(task).setDescription(description);
    verify(task).setTaskStatus(status);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(any(LocalDateTime.class));
    verify(kafkaTemplate).send(eq(ACHIEVEMENT_TOPIC), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
  }

  @Test
  void updateTask_shouldHandleLongTitleAndDescription() {
    when(taskDto.title()).thenReturn("A".repeat(255));
    when(taskDto.description()).thenReturn("B".repeat(1000));
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.updateTask(taskDto, taskId);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskRepository).findById(taskId);
    verify(task).setTitle("A".repeat(255));
    verify(task).setDescription("B".repeat(1000));
    verify(task).setTaskStatus(status);
    verify(task).setPriority(HIGH);
    verify(task).setExpirationDate(expirationDate);
    verify(task).setApprovedAt(any(LocalDateTime.class));
    verify(kafkaTemplate).send(eq(ACHIEVEMENT_TOPIC), any(KafkaTaskCompletionDto.class));
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
  }

  @Test
  void createTaskForUser_shouldCreateAndReturnTaskDto() {
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskFactory.createTaskFromDto(taskDto)).thenReturn(task);
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.createTaskForUser(taskDto);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskFactory).createTaskFromDto(taskDto);
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void createTaskForUser_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    doThrow(new IllegalArgumentException("Invalid task title")).when(taskValidator).validateTaskDto(taskDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> taskService.createTaskForUser(taskDto));
    assertEquals("Invalid task title", exception.getMessage());
    verify(taskValidator).validateTaskDto(taskDto);
    verifyNoInteractions(taskFactory, taskRepository, kafkaTemplate);
  }

  @Test
  void createTaskForUser_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskFactory.createTaskFromDto(taskDto)).thenReturn(task);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskRepository).save(task);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskService.createTaskForUser(taskDto));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskFactory).createTaskFromDto(taskDto);
    verify(taskRepository).save(task);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void createTaskForUser_shouldHandleLongTitleAndDescription() {
    when(taskDto.title()).thenReturn("A".repeat(255));
    when(taskDto.description()).thenReturn("B".repeat(1000));
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskFactory.createTaskFromDto(taskDto)).thenReturn(task);
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.createTaskForUser(taskDto);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskFactory).createTaskFromDto(taskDto);
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void createTaskForUser_shouldHandleNullExpirationDate() {
    when(taskDto.expirationDate()).thenReturn(null);
    doNothing().when(taskValidator).validateTaskDto(taskDto);
    when(taskFactory.createTaskFromDto(taskDto)).thenReturn(task);
    when(taskRepository.save(task)).thenReturn(task);
    TaskDto result = taskService.createTaskForUser(taskDto);
    assertNotNull(result);
    assertEquals(taskDto, result);
    verify(taskValidator).validateTaskDto(taskDto);
    verify(taskFactory).createTaskFromDto(taskDto);
    verify(taskRepository).save(task);
    verify(taskMapper).toDto(task);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void deleteTaskById_shouldDeleteTaskWhenExists() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    doNothing().when(taskRepository).delete(task);
    taskService.deleteTaskById(taskId);
    verify(taskRepository).findById(taskId);
    verify(taskRepository).delete(task);
    verifyNoInteractions(taskMapper, kafkaTemplate);
  }

  @Test
  void deleteTaskById_shouldThrowResourceNotFoundExceptionWhenTaskNotFound() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTaskById(taskId));
    assertEquals(TASK_NOT_FOUND_WITH_ID + taskId, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verifyNoInteractions(taskMapper, kafkaTemplate);
  }

  @Test
  void deleteTaskById_shouldThrowResourceNotFoundExceptionWhenIdIsZero() {
    when(taskRepository.findById(0L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTaskById(0L));
    assertEquals(TASK_NOT_FOUND_WITH_ID + 0L, exception.getMessage());
    verify(taskRepository).findById(0L);
    verifyNoInteractions(taskMapper, kafkaTemplate);
  }

  @Test
  void deleteTaskById_shouldHandleNegativeId() {
    when(taskRepository.findById(-1L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTaskById(-1L));
    assertEquals(TASK_NOT_FOUND_WITH_ID + -1L, exception.getMessage());
    verify(taskRepository).findById(-1L);
    verifyNoInteractions(taskMapper, kafkaTemplate);
  }

  @Test
  void getAllTasksAssignedToUser_shouldReturnPagedTasksWhenTasksExist() {
    Page<Task> taskPage = new PageImpl<>(List.of(task));
    when(taskRepository.findTasksAssignedToUser(slug, projectName, teamName, pageable)).thenReturn(taskPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedToUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(taskDto, result.getContent().get(0));
    verify(taskRepository).findTasksAssignedToUser(slug, projectName, teamName, pageable);
    verify(taskMapper).toDto(task);
  }

  @Test
  void getAllTasksAssignedToUser_shouldReturnEmptyPageWhenNoTasks() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedToUser(slug, projectName, teamName, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedToUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedToUser(slug, projectName, teamName, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedToUser_shouldHandleEmptySlug() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedToUser("", projectName, teamName, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedToUser("", projectName, teamName, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedToUser("", projectName, teamName, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedToUser_shouldHandleNullProjectAndTeamNames() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedToUser(slug, null, null, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedToUser(slug, null, null, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedToUser(slug, null, null, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedToUser_shouldHandleMultipleTasks() {
    Task task2 = mock(Task.class);
    TaskDto taskDto2 = mock(TaskDto.class);
    Page<Task> taskPage = new PageImpl<>(List.of(task, task2));
    when(taskRepository.findTasksAssignedToUser(slug, projectName, teamName, pageable)).thenReturn(taskPage);
    when(taskMapper.toDto(task2)).thenReturn(taskDto2);
    Page<TaskDto> result = taskService.getAllTasksAssignedToUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(taskDto, result.getContent().get(0));
    assertEquals(taskDto2, result.getContent().get(1));
    verify(taskRepository).findTasksAssignedToUser(slug, projectName, teamName, pageable);
    verify(taskMapper).toDto(task);
    verify(taskMapper).toDto(task2);
  }

  @Test
  void getAllTasksAssignedByUser_shouldReturnPagedTasksWhenTasksExist() {
    Page<Task> taskPage = new PageImpl<>(List.of(task));
    when(taskRepository.findTasksAssignedByUser(slug, projectName, teamName, pageable)).thenReturn(taskPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedByUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(taskDto, result.getContent().get(0));
    verify(taskRepository).findTasksAssignedByUser(slug, projectName, teamName, pageable);
    verify(taskMapper).toDto(task);
  }

  @Test
  void getAllTasksAssignedByUser_shouldReturnEmptyPageWhenNoTasks() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedByUser(slug, projectName, teamName, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedByUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedByUser(slug, projectName, teamName, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedByUser_shouldHandleEmptySlug() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedByUser("", projectName, teamName, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedByUser("", projectName, teamName, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedByUser("", projectName, teamName, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedByUser_shouldHandleNullProjectAndTeamNames() {
    Page<Task> emptyPage = new PageImpl<>(Collections.emptyList());
    when(taskRepository.findTasksAssignedByUser(slug, null, null, pageable)).thenReturn(emptyPage);
    Page<TaskDto> result = taskService.getAllTasksAssignedByUser(slug, null, null, pageable);
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    verify(taskRepository).findTasksAssignedByUser(slug, null, null, pageable);
    verifyNoInteractions(taskMapper);
  }

  @Test
  void getAllTasksAssignedByUser_shouldHandleMultipleTasks() {
    Task task2 = mock(Task.class);
    TaskDto taskDto2 = mock(TaskDto.class);
    Page<Task> taskPage = new PageImpl<>(List.of(task, task2));
    when(taskRepository.findTasksAssignedByUser(slug, projectName, teamName, pageable)).thenReturn(taskPage);
    when(taskMapper.toDto(task2)).thenReturn(taskDto2);
    Page<TaskDto> result = taskService.getAllTasksAssignedByUser(slug, projectName, teamName, pageable);
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(taskDto, result.getContent().get(0));
    assertEquals(taskDto2, result.getContent().get(1));
    verify(taskRepository).findTasksAssignedByUser(slug, projectName, teamName, pageable);
    verify(taskMapper).toDto(task);
    verify(taskMapper).toDto(task2);
  }

  @Test
  void uploadImage_shouldUploadImageAndSaveTask() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(imageService.uploadTaskImage(taskImageDto)).thenReturn(imageName);
    when(taskRepository.save(task)).thenReturn(task);
    taskService.uploadImage(taskId, taskImageDto);
    verify(taskRepository).findById(taskId);
    verify(imageService).uploadTaskImage(taskImageDto);
    verify(task).getImages();
    verify(taskRepository).save(task);
  }

  @Test
  void uploadImage_shouldThrowResourceNotFoundExceptionWhenTaskNotFound() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.uploadImage(taskId, taskImageDto));
    assertEquals(TASK_NOT_FOUND_WITH_ID + taskId, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verifyNoInteractions(imageService, taskMapper);
  }

  @Test
  void uploadImage_shouldHandleEmptyImageList() {
    when(task.getImages()).thenReturn(new ArrayList<>());
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(imageService.uploadTaskImage(taskImageDto)).thenReturn(imageName);
    when(taskRepository.save(task)).thenReturn(task);
    taskService.uploadImage(taskId, taskImageDto);
    verify(taskRepository).findById(taskId);
    verify(imageService).uploadTaskImage(taskImageDto);
    verify(task).getImages();
    verify(taskRepository).save(task);
  }

  @Test
  void uploadImage_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(imageService.uploadTaskImage(taskImageDto)).thenReturn(imageName);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskRepository).save(task);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskService.uploadImage(taskId, taskImageDto));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskRepository).findById(taskId);
    verify(imageService).uploadTaskImage(taskImageDto);
    verify(task).getImages();
    verify(taskRepository).save(task);
  }

  @Test
  void uploadImage_shouldHandleZeroTaskId() {
    when(taskRepository.findById(0L)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.uploadImage(0L, taskImageDto));
    assertEquals(TASK_NOT_FOUND_WITH_ID + 0L, exception.getMessage());
    verify(taskRepository).findById(0L);
    verifyNoInteractions(imageService, taskMapper);
  }

  @Test
  void updateImage_shouldThrowImageProcessingExceptionWhenImageNotFound() {
    when(task.getImages()).thenReturn(new ArrayList<>(Collections.emptyList()));
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> taskService.updateImage(taskId, taskImageDto, imageName));
    assertEquals(NO_IMAGE_TO_UPDATE, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verify(task).getImages();
    verifyNoInteractions(imageService, taskMapper);
  }

  @Test
  void deleteImage_shouldDeleteImageAndSaveTask() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    doNothing().when(imageService).deleteImage(imageName);
    when(taskRepository.save(task)).thenReturn(task);
    taskService.deleteImage(taskId, imageName);
    verify(taskRepository).findById(taskId);
    verify(imageService).deleteImage(imageName);
    verify(task).getImages();
    verify(taskRepository).save(task);
  }

  @Test
  void deleteImage_shouldThrowImageProcessingExceptionWhenImageNotFound() {
    when(task.getImages()).thenReturn(new ArrayList<>(List.of("other-image.jpg")));
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> taskService.deleteImage(taskId, imageName));
    assertEquals(NO_IMAGE_TO_DELETE, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verify(task).getImages();
    verifyNoInteractions(imageService, taskMapper);
  }

  @Test
  void deleteImage_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    doNothing().when(imageService).deleteImage(imageName);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(taskRepository).save(task);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> taskService.deleteImage(taskId, imageName));
    assertEquals("Constraint violation", exception.getMessage());
    verify(taskRepository).findById(taskId);
    verify(imageService).deleteImage(imageName);
    verify(task).getImages();
    verify(taskRepository).save(task);
  }

  @Test
  void deleteImage_shouldHandleEmptyImageList() {
    when(task.getImages()).thenReturn(new ArrayList<>());
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> taskService.deleteImage(taskId, imageName));
    assertEquals(NO_IMAGE_TO_DELETE, exception.getMessage());
    verify(taskRepository).findById(taskId);
    verify(task).getImages();
    verifyNoInteractions(imageService, taskMapper);
  }

  @Test
  void findAllSoonExpiringTasks_shouldReturnMappedDtosWithinDuration() {
    Duration duration = Duration.ofDays(3);
    LocalDateTime now = LocalDateTime.of(2025, 6, 18, 10, 0);
    LocalDateTime later = now.plus(duration);

    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(42L);
    when(userService.getUserByUsername(username)).thenReturn(mockUser);

    Task task1 = mock(Task.class);
    Task task2 = mock(Task.class);

    List<Task> taskList = List.of(task1, task2);
    when(taskRepository.findExpiringTasksForUser(any(), any(), eq(projectName), eq(teamName), eq(42L))).thenReturn(taskList);

    TaskDto dto1 = mock(TaskDto.class);
    TaskDto dto2 = mock(TaskDto.class);
    when(taskMapper.toDto(task1)).thenReturn(dto1);
    when(taskMapper.toDto(task2)).thenReturn(dto2);

    try (MockedStatic<LocalDateTime> mockedNow = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
      mockedNow.when(LocalDateTime::now).thenReturn(now);
      List<TaskDto> result = taskService.findAllSoonExpiringTasks(username, duration, projectName, teamName);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.containsAll(List.of(dto1, dto2)));

      verify(userService).getUserByUsername(username);
      verify(taskRepository).findExpiringTasksForUser(now, later, projectName, teamName, 42L);
      verify(taskMapper).toDto(task1);
      verify(taskMapper).toDto(task2);
    }
  }

  @Test
  void findAllSoonExpiringTasks_shouldReturnEmptyListIfNoTasksFound() {
    Duration duration = Duration.ofDays(2);
    LocalDateTime now = LocalDateTime.of(2025, 6, 18, 10, 0);

    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(99L);
    when(userService.getUserByUsername(username)).thenReturn(mockUser);
    when(taskRepository.findExpiringTasksForUser(any(), any(), any(), any(), eq(99L))).thenReturn(List.of());

    try (MockedStatic<LocalDateTime> mockedNow = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
      mockedNow.when(LocalDateTime::now).thenReturn(now);
      List<TaskDto> result = taskService.findAllSoonExpiringTasks(username, duration, "AnyProject", "AnyTeam");
      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(userService).getUserByUsername(username);
      verify(taskRepository).findExpiringTasksForUser(eq(now), eq(now.plus(duration)), any(), any(), eq(99L));
    }
  }
}