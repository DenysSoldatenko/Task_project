package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.TASK_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.tasks.TaskStatus;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TaskCommentFactoryTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TaskCommentRepository commentRepository;

  @InjectMocks
  private TaskCommentFactory factory;

  private Task task;
  private User sender;
  private User receiver;
  private UserDto senderDto;
  private UserDto receiverDto;
  private TaskDto taskDto;
  private TaskCommentDto taskCommentDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    senderDto = new UserDto(1L, "sender", "sender@gmail.com", "", "", null);
    receiverDto = new UserDto(2L, "receiver", "receiver@gmail.com", "", "", null);
    ProjectDto projectDto = new ProjectDto(1L, "P", "desc", senderDto);
    TeamDto teamDto = new TeamDto(1L, "T", "desc", senderDto);
    taskDto = new TaskDto(1L, projectDto, teamDto, "T", "D", now(), now().plusDays(5), now().plusDays(3), TaskStatus.APPROVED, TaskPriority.CRITICAL, receiverDto, senderDto, null);
    taskCommentDto = new TaskCommentDto(1L, taskDto, senderDto, receiverDto, null, "Message", now());

    task = Task.builder().id(1L).build();

    sender = new User();
    sender.setId(1L);
    sender.setUsername("sender@gmail.com");

    receiver = new User();
    receiver.setId(2L);
    receiver.setUsername("receiver@gmail.com");
  }

  @Test
  void shouldCreateTaskCommentWithGeneratedSlug() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(Collections.emptyList());
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(Collections.emptyList());

    TaskComment comment = factory.createTaskCommentFromDto(taskCommentDto);

    assertNotNull(comment);
    assertTrue(comment.getSlug().startsWith("task-1-"));
    assertEquals("Message", comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldReuseExistingSlugFromSender() {
    TaskComment existingComment = TaskComment.builder().slug("existing-slug").task(task).sender(sender).receiver(receiver).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(List.of(existingComment));
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(Collections.emptyList());

    TaskComment comment = factory.createTaskCommentFromDto(taskCommentDto);

    assertEquals("existing-slug", comment.getSlug());
    assertEquals("Message", comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldReuseExistingSlugFromReceiver() {
    TaskComment existingComment = TaskComment.builder().slug("existing-slug").task(task).sender(receiver).receiver(sender).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(Collections.emptyList());
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(List.of(existingComment));

    TaskComment comment = factory.createTaskCommentFromDto(taskCommentDto);

    assertEquals("existing-slug", comment.getSlug());
    assertEquals("Message", comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldThrowIfTaskNotFound() {
    when(taskRepository.findById(1L)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> factory.createTaskCommentFromDto(taskCommentDto));
    assertEquals(TASK_NOT_FOUND_WITH_ID + "1", ex.getMessage());
  }

  @Test
  void shouldThrowIfSenderNotFound() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> factory.createTaskCommentFromDto(taskCommentDto));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "sender@gmail.com", ex.getMessage());
  }

  @Test
  void shouldThrowIfReceiverNotFound() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> factory.createTaskCommentFromDto(taskCommentDto));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "receiver@gmail.com", ex.getMessage());
  }

  @Test
  void shouldCreateTaskCommentWithEmptyMessage() {
    TaskCommentDto emptyMessageDto = new TaskCommentDto(1L, taskDto, senderDto, receiverDto, null, "", now());

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(Collections.emptyList());
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(Collections.emptyList());

    TaskComment comment = factory.createTaskCommentFromDto(emptyMessageDto);

    assertNotNull(comment);
    assertTrue(comment.getSlug().startsWith("task-1-"));
    assertEquals("", comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldCreateTaskCommentWithNullMessage() {
    TaskCommentDto nullMessageDto = new TaskCommentDto(1L, taskDto, senderDto, receiverDto, null, null, now());

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(Collections.emptyList());
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(Collections.emptyList());

    TaskComment comment = factory.createTaskCommentFromDto(nullMessageDto);

    assertNotNull(comment);
    assertTrue(comment.getSlug().startsWith("task-1-"));
    assertNull(comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void shouldCreateTaskCommentWithLongMessage() {
    String longMessage = "A".repeat(1000);
    TaskCommentDto longMessageDto = new TaskCommentDto(1L, taskDto, senderDto, receiverDto, null, longMessage, now());

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(commentRepository.findByTaskAndSender(task, sender)).thenReturn(Collections.emptyList());
    when(commentRepository.findByTaskAndSender(task, receiver)).thenReturn(Collections.emptyList());

    TaskComment comment = factory.createTaskCommentFromDto(longMessageDto);

    assertNotNull(comment);
    assertTrue(comment.getSlug().startsWith("task-1-"));
    assertEquals(longMessage, comment.getMessage());
    assertEquals(sender, comment.getSender());
    assertEquals(receiver, comment.getReceiver());
    assertEquals(task, comment.getTask());
    assertNotNull(comment.getCreatedAt());
  }
}