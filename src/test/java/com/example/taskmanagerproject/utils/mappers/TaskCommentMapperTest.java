package com.example.taskmanagerproject.utils.mappers;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TaskCommentMapperTest {

  @Mock
  private TaskMapper taskMapper;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private TaskCommentMapperImpl taskCommentMapper;

  private Task task;
  private User sender;
  private User receiver;
  private TaskDto taskDto;
  private UserDto senderDto;
  private UserDto receiverDto;
  private TaskComment taskComment;
  private TaskCommentDto taskCommentDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    task = new Task();
    task.setId(1L);

    sender = new User();
    sender.setId(1L);
    sender.setUsername("sender@gmail.com");

    receiver = new User();
    receiver.setId(2L);
    receiver.setUsername("receiver@gmail.com");

    taskComment = TaskComment.builder()
        .id(1L)
        .task(task)
        .sender(sender)
        .receiver(receiver)
        .message("Test Comment")
        .slug("task-1-abc123")
        .createdAt(LocalDateTime.now())
        .build();

    senderDto = new UserDto(1L, "sender", "sender@gmail.com", "", "", null);
    receiverDto = new UserDto(2L, "receiver", "receiver@gmail.com", "", "", null);
    ProjectDto projectDto = new ProjectDto(1L, "P", "desc", senderDto);
    TeamDto teamDto = new TeamDto(1L, "T", "desc", senderDto);
    taskDto = new TaskDto(1L, projectDto, teamDto, "T", "D", now(), now().plusDays(5), now().plusDays(3), TaskStatus.APPROVED, TaskPriority.CRITICAL, receiverDto, senderDto, null);
    taskCommentDto = new TaskCommentDto(1L, taskDto, senderDto, receiverDto, "task-1-abc123", "Test Comment", now());
  }

  @Test
  void shouldMapTaskCommentToDto() {
    when(taskMapper.toDto(task)).thenReturn(taskDto);
    when(userMapper.toDto(sender)).thenReturn(senderDto);
    when(userMapper.toDto(receiver)).thenReturn(receiverDto);

    TaskCommentDto result = taskCommentMapper.toDto(taskComment);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Test Comment", result.message());
    assertEquals(senderDto, result.sender());
    assertEquals(receiverDto, result.receiver());
    assertEquals(taskDto, result.task());
    assertEquals("task-1-abc123", result.slug());
    assertEquals(taskComment.getCreatedAt(), result.createdAt());
  }

  @Test
  void shouldMapTaskCommentDtoToEntity() {
    when(taskMapper.toEntity(taskDto)).thenReturn(task);
    when(userMapper.toEntity(senderDto)).thenReturn(sender);
    when(userMapper.toEntity(receiverDto)).thenReturn(receiver);

    TaskComment result = taskCommentMapper.toEntity(taskCommentDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Comment", result.getMessage());
    assertEquals(sender, result.getSender());
    assertEquals(receiver, result.getReceiver());
    assertEquals(task, result.getTask());
    assertEquals("task-1-abc123", result.getSlug());
    assertEquals(taskCommentDto.createdAt(), result.getCreatedAt());
  }

  @Test
  void shouldHandleNullTaskComment() {
    TaskCommentDto result = taskCommentMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullTaskCommentDto() {
    TaskComment result = taskCommentMapper.toEntity(null);
    assertNull(result);
  }
}