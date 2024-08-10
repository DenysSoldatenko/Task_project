package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.TaskStatus.NOT_STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.utils.mappers.TaskMapper;
import com.example.taskmanagerproject.repositories.TaskRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("Task Service Tests")
class TaskServiceImplTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskMapper taskMapper;

  @InjectMocks
  private TaskServiceImpl taskService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("Get Task By Id - Success")
  void getTaskById_Success() {
    Long taskId = 1L;
    Task task = new Task();
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskMapper.toDto(task)).thenReturn(
      new TaskDto(1L, "Buy groceries",
        "Get milk, bread, and eggs", NOT_STARTED,
        LocalDateTime.now(), Collections.singletonList(""))
    );

    TaskDto result = taskService.getTaskById(taskId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Get Task By Id - Task Not Found")
  void getTaskById_TaskNotFound() {
    Long taskId = 1L;
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(taskId));
  }

  @Test
  @DisplayName("Get All Tasks By User Id - Success")
  void getAllTasksByUserId_Success() {
    Long userId = 1L;
    List<Task> tasks = new ArrayList<>();
    when(taskRepository.findAllTasksByUserId(userId)).thenReturn(tasks);
    when(taskMapper.toDto(any())).thenReturn(
      new TaskDto(1L, "Buy groceries",
        "Get milk, bread, and eggs", NOT_STARTED,
        LocalDateTime.now(), Collections.singletonList("")));

    List<TaskDto> result = taskService.getAllTasksByUserId(userId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Update Task - Success")
  void updateTask_Success() {
    Long taskId = 1L;
    TaskDto taskDto = new TaskDto(1L, "Buy groceries",
      "Get milk, bread, and eggs", NOT_STARTED,
      LocalDateTime.now(), Collections.singletonList(""));
    Task task = new Task();
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenReturn(task);
    when(taskMapper.toDto(task)).thenReturn(taskDto);

    TaskDto result = taskService.updateTask(taskDto, taskId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Delete Task By Id - Success")
  void deleteTaskById_Success() {
    Long taskId = 1L;
    Task task = new Task();
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    assertDoesNotThrow(() -> taskService.deleteTaskById(taskId));

    verify(taskRepository, times(1)).delete(task);
  }
}
