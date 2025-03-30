package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.utils.MessageUtil.TASK_COMMENT_INVALID_SENDER_RECEIVER;
import static com.example.taskmanagerproject.utils.MessageUtil.TASK_NOT_FOUND_WITH_ID;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskCommentValidatorTest {

  private Validator validator;
  private TaskRepository taskRepository;
  private TaskCommentValidator taskCommentValidator;

  private final UserDto sender = new UserDto(1L, "sender@gmail.com", "Sender", "sender-slug", "", List.of(""));
  private final UserDto receiver = new UserDto(2L, "receiver@gmail.com", "Receiver", "receiver-slug", "", List.of(""));
  private final ProjectDto projectDto = new ProjectDto(100L, "New Project", "Description", sender);
  private final TeamDto teamDto = new TeamDto(100L, "New Team", "Description", sender);
  private final TaskDto task = new TaskDto(1L, projectDto, teamDto, "Task", "Description", now(), now().plusDays(5), now().plusDays(3), APPROVED, CRITICAL, sender, receiver, null);
  private final TaskCommentDto validTaskCommentDto = new TaskCommentDto(1L, task, sender, receiver, "task-slug", "Message", now());

  @BeforeEach
  void setUp() {
    validator = mock(Validator.class);
    taskRepository = mock(TaskRepository.class);
    taskCommentValidator = new TaskCommentValidator(validator, taskRepository);
  }

  @Test
  void validateTaskCommentDto_shouldPassWithValidInput() {
    when(validator.validate(validTaskCommentDto)).thenReturn(Collections.emptySet());
    when(taskRepository.findById(1L)).thenReturn(Optional.of(new com.example.taskmanagerproject.entities.tasks.Task()));

    assertDoesNotThrow(() -> taskCommentValidator.validateTaskCommentDto(validTaskCommentDto));
  }

  @Test
  void validateTaskCommentDto_shouldThrowIfTaskNotFound() {
    when(validator.validate(validTaskCommentDto)).thenReturn(Collections.emptySet());
    when(taskRepository.findById(1L)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> taskCommentValidator.validateTaskCommentDto(validTaskCommentDto));
    assertTrue(ex.getMessage().contains(TASK_NOT_FOUND_WITH_ID + "1"));
  }

  @Test
  void validateTaskCommentDto_shouldThrowIfInvalidSenderReceiver() {
    UserDto invalidSender = new UserDto(3L, "invalid@gmail.com", "Invalid", "invalid-slug", "", List.of(""));
    TaskCommentDto invalidDto = new TaskCommentDto(1L, task, invalidSender, receiver, "task-slug", "Message", now());

    when(validator.validate(invalidDto)).thenReturn(Collections.emptySet());
    when(taskRepository.findById(1L)).thenReturn(Optional.of(new com.example.taskmanagerproject.entities.tasks.Task()));

    ValidationException ex = assertThrows(ValidationException.class, () -> taskCommentValidator.validateTaskCommentDto(invalidDto));
    assertTrue(ex.getMessage().contains(TASK_COMMENT_INVALID_SENDER_RECEIVER));
  }

  @Test
  void validateTaskCommentDto_shouldThrowIfConstraintViolationsExist() {
    ConstraintViolation<TaskCommentDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Message must not be blank");

    when(validator.validate(validTaskCommentDto)).thenReturn(Set.of(violation));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task()));

    ValidationException ex = assertThrows(ValidationException.class, () -> taskCommentValidator.validateTaskCommentDto(validTaskCommentDto));
    assertTrue(ex.getMessage().contains("Message must not be blank"));
  }

  @Test
  void validateTaskCommentDto_shouldThrowMultipleErrors() {
    ConstraintViolation<TaskCommentDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Message must not be blank");

    UserDto invalidSender = new UserDto(3L, "invalid@gmail.com", "Invalid", "invalid-slug", "", List.of(""));
    TaskCommentDto invalidDto = new TaskCommentDto(1L, task, invalidSender, receiver, "task-slug", "Message", now());

    when(validator.validate(invalidDto)).thenReturn(Set.of(violation));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(new com.example.taskmanagerproject.entities.tasks.Task()));

    ValidationException ex = assertThrows(ValidationException.class, () -> taskCommentValidator.validateTaskCommentDto(invalidDto));
    assertTrue(ex.getMessage().contains("Message must not be blank"));
    assertTrue(ex.getMessage().contains(TASK_COMMENT_INVALID_SENDER_RECEIVER));
  }
}