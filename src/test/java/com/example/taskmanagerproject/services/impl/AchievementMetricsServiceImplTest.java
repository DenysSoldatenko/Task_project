package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementMetricsServiceImplTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskCommentRepository taskCommentRepository;

  @InjectMocks
  private AchievementMetricsServiceImpl service;

  private final Long taskId = 1L;
  private final Long userId = 1L;
  private final Long teamId = 3L;
  private final Long projectId = 2L;

  private KafkaTaskCompletionDto event;

  @BeforeEach
  void setUp() {
    event = new KafkaTaskCompletionDto(taskId, userId, teamId, projectId);
  }

  private Task createTask(LocalDateTime createdAt, LocalDateTime approvedAt, LocalDateTime expirationDate, TaskPriority taskPriority, Long teamId) {
    Task task = new Task();
    task.setId(new Random().nextLong());
    task.setCreatedAt(createdAt);
    task.setApprovedAt(approvedAt);
    task.setExpirationDate(expirationDate);
    task.setPriority(taskPriority);
    Team team = new Team();
    team.setId(teamId);
    task.setTeam(team);
    return task;
  }

  @Test
  void countApprovedTasks_shouldReturnCorrectCountWhenMultipleTasksExist() {
    var now = LocalDateTime.now();
    List<Task> tasks = List.of(
        createTask(now.minusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId),
        createTask(now.minusDays(2), now, now.plusDays(5), TaskPriority.MEDIUM, teamId),
        createTask(now.minusDays(3), now, now.plusDays(5), TaskPriority.MEDIUM, teamId)
    );
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    long result = service.countApprovedTasks(event);
    assertEquals(3, result);
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void countApprovedTasks_shouldReturnOneWhenSingleTaskExists() {
    var now = LocalDateTime.now();
    List<Task> tasks = List.of(
        createTask(now.minusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId)
    );
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    long result = service.countApprovedTasks(event);
    assertEquals(1, result);
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void countApprovedTasks_shouldReturnZeroWhenNoTasksExist() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    long result = service.countApprovedTasks(event);
    assertEquals(0, result);
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnTrueWhenEnoughRecentTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 35)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i).plusSeconds(1), // Buffer to ensure within 30 days
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnTrueWhenExactly30RecentTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i).plusSeconds(1),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnFalseWhenFewerThan30RecentTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 29)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i).plusSeconds(1),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnFalseWhenNoRecentTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 35)
        .mapToObj(i -> createTask(
          now.minusDays(i + 31),
          now.minusDays(i + 31), // Before 30 days
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnFalseWhenTasksApprovedAtBoundary() {
    var now = LocalDateTime.now();
    var boundary = now.minusDays(30);
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          boundary,
          boundary, // Exactly at 30-day boundary
          boundary.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksInLast30Days_shouldReturnTrueWhenMixedRecentAndOldTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 30 recent tasks
    IntStream.range(0, 30)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i),
          now.minusDays(i).plusSeconds(1),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        )));
    // 10 older tasks
    IntStream.range(0, 10)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i + 31),
          now.minusDays(i + 31),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        )));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksInLast30Days(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnTrueWhenEnoughTasksBeforeDeadline() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 25)
        .mapToObj(i -> createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i).plusDays(5).plusSeconds(1), // Approved before expiration
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnTrueWhenExactly20TasksBeforeDeadline() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 20)
        .mapToObj(i -> createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i).plusDays(5).plusSeconds(1),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnFalseWhenFewerThan20TasksBeforeDeadline() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 19)
        .mapToObj(i -> createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i).plusDays(5).plusSeconds(1),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnFalseWhenTasksApprovedAfterDeadline() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 25)
        .mapToObj(i -> createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i).minusDays(1), // Approved after expiration
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnFalseWhenTasksApprovedAtDeadline() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 20)
        .mapToObj(i -> createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i), // Approved at expiration
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksBeforeDeadline_shouldReturnTrueWhenMixedTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 20 tasks approved before deadline
    IntStream.range(0, 20)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i + 10),
          now.minusDays(i),
          now.minusDays(i).plusDays(5).plusSeconds(1),
          TaskPriority.MEDIUM,
          teamId
        )));
    // 10 tasks approved after deadline
    IntStream.range(0, 10)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i + 20),
          now.minusDays(i),
          now.minusDays(i).minusDays(1),
          TaskPriority.MEDIUM,
          teamId
        )));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksBeforeDeadline(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnTrueWhenEnoughHighPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 25)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnTrueWhenExactly20HighPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 20)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnFalseWhenFewerThan20HighPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 19)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnFalseWhenNoHighPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 25)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedHighPriorityTasks_shouldReturnTrueWhenMixedPriorities() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 20 HIGH priority tasks
    IntStream.range(0, 20)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        )));
    // 10 MEDIUM priority tasks
    IntStream.range(0, 10)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i + 20),
          now.minusDays(i + 20),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        )));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedHighPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnTrueWhenEnoughCriticalPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 45)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnTrueWhenExactly40CriticalPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 40)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnFalseWhenFewerThan40CriticalPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 39)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnFalseWhenNoCriticalPriorityTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 45)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        ))
        .toList();
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedCriticalPriorityTasks_shouldReturnTrueWhenMixedPriorities() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 40 CRITICAL priority tasks
    IntStream.range(0, 40)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        )));
    // 10 HIGH priority tasks
    IntStream.range(0, 10)
        .forEach(i -> tasks.add(createTask(
          now.minusDays(i + 40),
          now.minusDays(i + 40),
          now.plusDays(5),
          TaskPriority.HIGH,
          teamId
        )));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedCriticalPriorityTasks(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnTrueWhenOneDayHasMoreThanFiveTasks() {
    var now = LocalDateTime.now();
    var targetDay = now.minusDays(1).toLocalDate().atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 6 tasks on target day
    for (int i = 0; i < 6; i++) {
      tasks.add(createTask(
          targetDay.minusDays(1),
          targetDay.plusHours(i),
          targetDay.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnTrueWhenOneDayHasExactlyFiveTasks() {
    var now = LocalDateTime.now();
    var targetDay = now.minusDays(1).toLocalDate().atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 5 tasks on target day
    for (int i = 0; i < 5; i++) {
      tasks.add(createTask(
          targetDay.minusDays(1),
          targetDay.plusHours(i),
          targetDay.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnFalseWhenNoDayHasFiveTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 4 tasks per day for 3 days
    for (int day = 0; day < 3; day++) {
      var targetDay = now.minusDays(day).toLocalDate().atStartOfDay();
      for (int i = 0; i < 4; i++) {
        tasks.add(createTask(
            targetDay.minusDays(1),
            targetDay.plusHours(i),
            targetDay.plusDays(5),
            TaskPriority.MEDIUM,
            teamId
        ));
      }
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnTrueWhenMultipleDaysWithOneDayMeetingThreshold() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 5 tasks on target day
    var targetDay = now.minusDays(1).toLocalDate().atStartOfDay();
    for (int i = 0; i < 5; i++) {
      tasks.add(createTask(
          targetDay.minusDays(1),
          targetDay.plusHours(i),
          targetDay.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
      ));
    }
    // 3 tasks on another day
    var otherDay = now.minusDays(2).toLocalDate().atStartOfDay();
    for (int i = 0; i < 3; i++) {
      tasks.add(createTask(
          otherDay.minusDays(1),
          otherDay.plusHours(i),
          otherDay.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnTrueWhenTasksSpanMidnightBoundary() {
    var now = LocalDateTime.now();
    var targetDay = now.minusDays(1).toLocalDate().atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 5 tasks on target day, including near midnight
    tasks.add(createTask(targetDay.minusDays(1), targetDay.plusHours(0), targetDay.plusDays(5), TaskPriority.MEDIUM, teamId));
    tasks.add(createTask(targetDay.minusDays(1), targetDay.plusHours(12), targetDay.plusDays(5), TaskPriority.MEDIUM, teamId));
    tasks.add(createTask(targetDay.minusDays(1), targetDay.plusHours(23).plusMinutes(59), targetDay.plusDays(5), TaskPriority.MEDIUM, teamId));
    tasks.add(createTask(targetDay.minusDays(1), targetDay.plusHours(23).plusMinutes(59).plusSeconds(59), targetDay.plusDays(5), TaskPriority.MEDIUM, teamId));
    tasks.add(createTask(targetDay.minusDays(1), targetDay.plusHours(1), targetDay.plusDays(5), TaskPriority.MEDIUM, teamId));
    // 1 task on next day (different date)
    tasks.add(createTask(targetDay, targetDay.plusDays(1).plusMinutes(1), targetDay.plusDays(6), TaskPriority.MEDIUM, teamId));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasApprovedTasksDaily_shouldReturnFalseWhenOneTaskPerDay() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 1 task per day for 10 days
    for (int day = 0; day < 10; day++) {
      var targetDay = now.minusDays(day).toLocalDate().atStartOfDay();
      tasks.add(createTask(
          targetDay.minusDays(1),
          targetDay.plusHours(12),
          targetDay.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedTasksDaily(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnTrueWhenEnoughCancelledTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 15)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskRepository.hasTaskBeenCancelled(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskRepository).hasTaskBeenCancelled(task.getId()));
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnTrueWhenExactlyTenCancelledTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 10)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskRepository.hasTaskBeenCancelled(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskRepository).hasTaskBeenCancelled(task.getId()));
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnFalseWhenFewerThanTenCancelledTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 9)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskRepository.hasTaskBeenCancelled(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskRepository).hasTaskBeenCancelled(task.getId()));
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnFalseWhenNoCancelledTasks() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 15)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskRepository.hasTaskBeenCancelled(task.getId())).thenReturn(false));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskRepository).hasTaskBeenCancelled(task.getId()));
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasTasksApprovedAfterRejection_shouldReturnTrueWhenMixedCancelledAndNonCancelledTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 10 cancelled tasks
    IntStream.range(0, 10)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // 5 non-cancelled tasks
    IntStream.range(10, 15)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // Mock cancellation status
    IntStream.range(0, tasks.size()).forEach(i ->
        when(taskRepository.hasTaskBeenCancelled(tasks.get(i).getId())).thenReturn(i < 10)
    );
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasTasksApprovedAfterRejection(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskRepository).hasTaskBeenCancelled(task.getId()));
  }




  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnTrueWhenOneMonthHasMoreThanTwentyCriticalTasks() {
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 25 CRITICAL tasks in target month
    for (int i = 0; i < 25; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnTrueWhenOneMonthHasExactlyTwentyCriticalTasks() {
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 20 CRITICAL tasks in target month
    for (int i = 0; i < 20; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnFalseWhenNoMonthHasTwentyCriticalTasks() {
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 19 CRITICAL tasks in target month
    for (int i = 0; i < 19; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnFalseWhenNoCriticalTasks() {
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 25 HIGH priority tasks
    for (int i = 0; i < 25; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.HIGH,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnTrueWhenMultipleMonthsWithOneMonthMeetingThreshold() {
    List<Task> tasks = new ArrayList<>();
    // 20 CRITICAL tasks in target month
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    for (int i = 0; i < 20; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    // 15 CRITICAL tasks in another month
    var otherMonth = YearMonth.now().minusMonths(2);
    var otherDate = otherMonth.atDay(1).atStartOfDay();
    for (int i = 0; i < 15; i++) {
      tasks.add(createTask(
          otherDate.minusDays(1),
          otherDate.plusDays(i),
          otherDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnTrueWhenTasksSpanMonthBoundary() {
    var targetMonth = YearMonth.now().minusMonths(1);
    var targetDate = targetMonth.atDay(1).atStartOfDay();
    var nextMonth = targetMonth.plusMonths(1);
    var nextMonthDate = nextMonth.atDay(1).atStartOfDay();
    List<Task> tasks = new ArrayList<>();
    // 20 CRITICAL tasks in target month, including last day
    for (int i = 0; i < 20; i++) {
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(i),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    // 5 CRITICAL tasks in next month, including first day
    for (int i = 0; i < 5; i++) {
      tasks.add(createTask(
          nextMonthDate.minusDays(1),
          nextMonthDate.plusDays(i),
          nextMonthDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedCriticalBugsInOneMonth_shouldReturnFalseWhenOneCriticalTaskPerMonth() {
    List<Task> tasks = new ArrayList<>();
    // 1 CRITICAL task per month for 12 months
    for (int month = 0; month < 12; month++) {
      var targetMonth = YearMonth.now().minusMonths(month);
      var targetDate = targetMonth.atDay(1).atStartOfDay();
      tasks.add(createTask(
          targetDate.minusDays(1),
          targetDate.plusDays(15),
          targetDate.plusDays(30),
          TaskPriority.CRITICAL,
          teamId
      ));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasFixedCriticalBugsInOneMonth(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedBugs_shouldReturnTrueWhenEnoughTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 105)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasFixedBugs_shouldReturnTrueWhenExactlyOneHundredTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 100)
        .mapToObj(i -> createTask(
            now.minusDays(i),
            now.minusDays(i),
            now.plusDays(5),
            TaskPriority.MEDIUM,
            teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasFixedBugs_shouldReturnFalseWhenFewerThanOneHundredTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 99)
        .mapToObj(i -> createTask(
            now.minusDays(i),
            now.minusDays(i),
            now.plusDays(5),
            TaskPriority.MEDIUM,
            teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasFixedBugs_shouldReturnFalseWhenNoTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 105)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(false));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasFixedBugs_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasFixedBugs_shouldReturnTrueWhenMixedTasksWithAndWithoutComments() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 100 tasks with comments
    IntStream.range(0, 100)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // 10 tasks without comments
    IntStream.range(100, 110)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // Mock comment status
    IntStream.range(0, tasks.size()).forEach(i ->
        when(taskCommentRepository.existsByTaskId(tasks.get(i).getId())).thenReturn(i < 100)
    );
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasFixedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasReportedBugs_shouldReturnTrueWhenEnoughCriticalTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasReportedBugs_shouldReturnTrueWhenExactlyTwentyFiveCriticalTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 25)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasReportedBugs_shouldReturnFalseWhenFewerThanTwentyFiveCriticalTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 24)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasReportedBugs_shouldReturnFalseWhenNoCriticalTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(false));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasReportedBugs_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasReportedBugs_shouldReturnFalseWhenCriticalTasksWithoutComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.CRITICAL,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(false));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasReportedBugs(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasResolvedReviewComments_shouldReturnTrueWhenEnoughTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 35)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasResolvedReviewComments_shouldReturnTrueWhenExactlyThirtyTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasResolvedReviewComments_shouldReturnFalseWhenFewerThanThirtyTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 29)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(true));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasResolvedReviewComments_shouldReturnFalseWhenNoTasksWithComments() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 35)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();
    tasks.forEach(task -> when(taskCommentRepository.existsByTaskId(task.getId())).thenReturn(false));
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }

  @Test
  void hasResolvedReviewComments_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(new ArrayList<>());
    assertFalse(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
  }

  @Test
  void hasResolvedReviewComments_shouldReturnTrueWhenMixedTasksWithAndWithoutComments() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 30 tasks with comments
    IntStream.range(0, 30)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // 10 tasks without comments
    IntStream.range(30, 40)
        .mapToObj(i -> createTask(
          now.minusDays(i),
          now.minusDays(i),
          now.plusDays(5),
          TaskPriority.MEDIUM,
          teamId
        ))
        .forEach(tasks::add);
    // Mock comment status
    IntStream.range(0, tasks.size()).forEach(i ->
        when(taskCommentRepository.existsByTaskId(tasks.get(i).getId())).thenReturn(i < 30)
    );
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasResolvedReviewComments(event));
    verify(taskRepository).findAllCompletedTasksAssignedToUser(userId, projectId, teamId);
    tasks.forEach(task -> verify(taskCommentRepository).existsByTaskId(task.getId()));
  }




  @Test
  void hasApprovedTasks10PercentFaster_shouldReturnTrueWhenEnoughFastApprovals() {
    var tasks = new ArrayList<Task>();
    // Add 100 tasks with approval times 3 to 102 hours
    for (int i = 0; i < 100; i++) {
      var created = LocalDateTime.now().minusDays(10 + i);
      var approved = created.plusHours(i + 3);
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    // Add 25 tasks with 10% faster approvals (1 to 25 hours)
    for (int i = 0; i < 25; i++) {
      var created = LocalDateTime.now().minusDays(10 + i);
      var approved = created.plusHours(i + 1);
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertTrue(service.hasApprovedTasks10PercentFaster(event));
  }

  @Test
  void hasApprovedTasks10PercentFaster_shouldReturnTrueWhenExactlyTwentyFastApprovals() {
    var tasks = new ArrayList<Task>();
    // Add 100 tasks with approval times 3 to 102 hours
    for (int i = 0; i < 100; i++) {
      var created = LocalDateTime.now().minusDays(10 + i);
      var approved = created.plusHours(i + 3);
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    // Add 20 tasks with 10% faster approvals (1 to 20 hours)
    for (int i = 0; i < 20; i++) {
      var created = LocalDateTime.now().minusDays(10 + i);
      var approved = created.plusHours(i + 1);
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertTrue(service.hasApprovedTasks10PercentFaster(event));
  }

  @Test
  void hasApprovedTasks10PercentFaster_shouldReturnFalseWhenNoFastApprovals() {
    var tasks = new ArrayList<Task>();
    // Add 100 tasks with approval times all at 10 hours
    for (int i = 0; i < 100; i++) {
      var created = LocalDateTime.now().minusDays(10 + i);
      var approved = created.plusHours(10);
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasApprovedTasks10PercentFaster(event));
  }

  @Test
  void hasApprovedTasks10PercentFaster_shouldReturnFalseWhenNoTasks() {
    var tasks = new ArrayList<Task>();
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasApprovedTasks10PercentFaster(event));
  }

  @Test
  void hasApprovedTasks10PercentFaster_shouldReturnFalseWhenSingleTask() {
    var tasks = new ArrayList<Task>();
    var created = LocalDateTime.now().minusDays(10);
    var approved = created.plusHours(1);
    tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasApprovedTasks10PercentFaster(event));
  }

  @Test
  void hasMaintained90PercentOnTimeApprovalRate_shouldReturnTrueWhenExactly90PercentOnTime() {
    var now = LocalDateTime.now();
    var tasks = new ArrayList<Task>();

    // 90 on-time approvals
    for (int i = 0; i < 90; i++) {
      var created = now.minusDays(i + 1);
      var approved = created.plusHours(1); // before expiration
      var expiration = approved.plusHours(2);
      tasks.add(createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId));
    }

    // 10 late approvals
    for (int i = 0; i < 10; i++) {
      var created = now.minusDays(100 + i);
      var expiration = created.plusHours(1);
      var approved = expiration.plusHours(2); // after expiration
      tasks.add(createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasMaintained90PercentOnTimeApprovalRate(event));
  }

  @Test
  void hasMaintained90PercentOnTimeApprovalRate_shouldReturnFalseWhenLessThan90PercentOnTime() {
    var now = LocalDateTime.now();
    var tasks = new ArrayList<Task>();

    // 89 on-time approvals
    for (int i = 0; i < 89; i++) {
      var created = now.minusDays(i + 1);
      var approved = created.plusHours(1);
      var expiration = approved.plusHours(2);
      tasks.add(createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId));
    }

    // 11 late approvals
    for (int i = 0; i < 11; i++) {
      var created = now.minusDays(100 + i);
      var expiration = created.plusHours(1);
      var approved = expiration.plusHours(3);
      tasks.add(createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasMaintained90PercentOnTimeApprovalRate(event));
  }

  @Test
  void hasMaintained90PercentOnTimeApprovalRate_shouldReturnTrueWhenAllApprovalsOnTime() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 50)
        .mapToObj(i -> {
          var created = now.minusDays(i + 1);
          var approved = created.plusHours(1);
          var expiration = approved.plusHours(2);
          return createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId);
        })
        .toList();

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasMaintained90PercentOnTimeApprovalRate(event));
  }

  @Test
  void hasMaintained90PercentOnTimeApprovalRate_shouldReturnFalseWhenAllApprovalsLate() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 50)
        .mapToObj(i -> {
          var created = now.minusDays(i + 1);
          var expiration = created.plusHours(2);
          var approved = expiration.plusHours(5); // late
          return createTask(created, approved, expiration, TaskPriority.MEDIUM, teamId);
        })
        .toList();

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasMaintained90PercentOnTimeApprovalRate(event));
  }

  @Test
  void hasApprovedCriticalTaskWithin24Hours_shouldReturnTrueWhenCriticalTaskApprovedWithin24Hours() {
    var now = LocalDateTime.now();
    var created = now.minusHours(20);

    var task = createTask(created, now, now.plusDays(1), TaskPriority.CRITICAL, teamId);
    var tasks = List.of(task);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedCriticalTaskWithin24Hours(event));
  }

  @Test
  void hasApprovedCriticalTaskWithin24Hours_shouldReturnFalseWhenCriticalTaskApprovedAfter24Hours() {
    var now = LocalDateTime.now();
    var created = now.minusHours(30);

    var task = createTask(created, now, now.plusDays(1), TaskPriority.CRITICAL, teamId);
    var tasks = List.of(task);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedCriticalTaskWithin24Hours(event));
  }

  @Test
  void hasApprovedCriticalTaskWithin24Hours_shouldReturnFalseWhenNoCriticalTasksPresent() {
    var now = LocalDateTime.now();
    var task = createTask(now.minusHours(5), now, now.plusDays(1), TaskPriority.MEDIUM, teamId);
    var tasks = List.of(task);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasApprovedCriticalTaskWithin24Hours(event));
  }

  @Test
  void hasApprovedCriticalTaskWithin24Hours_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(List.of());
    assertFalse(service.hasApprovedCriticalTaskWithin24Hours(event));
  }

  @Test
  void hasApprovedCriticalTaskWithin24Hours_shouldReturnTrueWhenMultipleTasksAndOneIsFastCritical() {
    var now = LocalDateTime.now();

    var fastCritical = createTask(now.minusHours(5), now, now.plusDays(1), TaskPriority.CRITICAL, teamId);
    var slowCritical = createTask(now.minusDays(2), now, now.plusDays(1), TaskPriority.CRITICAL, teamId);
    var nonCritical = createTask(now.minusDays(1), now, now.plusDays(1), TaskPriority.MEDIUM, teamId);

    var tasks = List.of(slowCritical, nonCritical, fastCritical);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasApprovedCriticalTaskWithin24Hours(event));
  }

  @Test
  void hasSavedProjectByApprovingTaskJustBeforeDeadline_shouldReturnTrueWhenTaskApprovedWithin5MinutesOfDeadline() {
    var now = LocalDateTime.now();
    var expiration = now.minusMinutes(3); // approved just 3 mins before deadline

    var task = createTask(now.minusDays(1), now, expiration, TaskPriority.MEDIUM, teamId);
    var tasks = List.of(task);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasSavedProjectByApprovingTaskJustBeforeDeadline(event));
  }

  @Test
  void hasSavedProjectByApprovingTaskJustBeforeDeadline_shouldReturnFalseWhenApprovedTooEarly() {
    var now = LocalDateTime.now();
    var expiration = now.minusMinutes(10); // 10 mins before deadline

    var task = createTask(now.minusDays(1), now, expiration, TaskPriority.MEDIUM, teamId);
    var tasks = List.of(task);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertFalse(service.hasSavedProjectByApprovingTaskJustBeforeDeadline(event));
  }

  @Test
  void hasSavedProjectByApprovingTaskJustBeforeDeadline_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(List.of());
    assertFalse(service.hasSavedProjectByApprovingTaskJustBeforeDeadline(event));
  }

  @Test
  void hasSavedProjectByApprovingTaskJustBeforeDeadline_shouldReturnTrueWhenAtLeastOneTaskMatchesAmongMany() {
    var now = LocalDateTime.now();

    var regularTask = createTask(now.minusDays(3), now.minusDays(1), now.minusDays(2), TaskPriority.LOW, teamId);
    var lateTask = createTask(now.minusDays(2), now.minusHours(1), now.minusHours(2), TaskPriority.MEDIUM, teamId);
    var edgeTask = createTask(now.minusDays(1), now, now.minusMinutes(4), TaskPriority.HIGH, teamId); // eligible

    var tasks = List.of(regularTask, lateTask, edgeTask);

    when(taskRepository.findAllCompletedTasksAssignedToUser(userId, projectId, teamId)).thenReturn(tasks);
    assertTrue(service.hasSavedProjectByApprovingTaskJustBeforeDeadline(event));
  }



  @Test
  void hasCollaboratedWithMultipleTeams_shouldReturnTrueIfWorkedIn5Teams() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    for (long i = 1; i <= 5; i++) {
      tasks.add(createTask(now, now, now.plusDays(1), TaskPriority.MEDIUM, i));
    }
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertTrue(service.hasCollaboratedWithMultipleTeams(event));
  }

  @Test
  void hasCollaboratedWithMultipleTeams_shouldReturnFalseIfWorkedInLessThanFiveTeams() {
    var now = LocalDateTime.now();
    var tasks = LongStream.rangeClosed(1, 4)
        .mapToObj(teamId -> createTask(now, now, now.plusDays(1), TaskPriority.MEDIUM, teamId))
        .toList();

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);

    assertFalse(service.hasCollaboratedWithMultipleTeams(event));
  }

  @Test
  void hasCollaboratedWithMultipleTeams_shouldReturnFalseIfAllTasksFromSameTeam() {
    var now = LocalDateTime.now();
    var tasks = IntStream.range(0, 10)
        .mapToObj(i -> createTask(now.minusDays(i), now.minusDays(i), now.plusDays(1), TaskPriority.MEDIUM, 42L))
        .toList();

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);

    assertFalse(service.hasCollaboratedWithMultipleTeams(event));
  }

  @Test
  void hasCollaboratedWithMultipleTeams_shouldReturnFalseIfNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(List.of());

    assertFalse(service.hasCollaboratedWithMultipleTeams(event));
  }

  @Test
  void hasWorkedContinuouslyFor6Months_shouldReturnFalseWhenOneMonthHasNoTask() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      if (i == 2) {
        continue; // skip 3rd month ago
      }
      tasks.add(createTask(
          now.minusMonths(i).minusDays(5),
          now.minusMonths(i),
          now.minusMonths(i).plusDays(3),
          TaskPriority.MEDIUM,
          teamId
      ));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);

    assertFalse(service.hasWorkedContinuouslyFor6Months(event));
  }

  @Test
  void hasWorkedContinuouslyFor6Months_shouldReturnFalseWhenEarliestTaskIsTooRecent() {
    var now = LocalDateTime.now();
    List<Task> tasks = IntStream.range(0, 3)
        .mapToObj(i -> createTask(
          now.minusMonths(i),
          now.minusMonths(i),
          now.minusMonths(i).plusDays(1),
          TaskPriority.MEDIUM,
          teamId
        ))
        .toList();

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);

    assertFalse(service.hasWorkedContinuouslyFor6Months(event));
  }

  @Test
  void hasWorkedContinuouslyFor6Months_shouldReturnFalseWhenNoTasksExist() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(List.of());

    assertFalse(service.hasWorkedContinuouslyFor6Months(event));
  }

  @Test
  void hasCompletedLongDurationTasks_shouldReturnTrueWhenAtLeast50LongTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();

    // Add 50 tasks longer than 7 days
    for (int i = 0; i < 50; i++) {
      var created = now.minusDays(20 + i);
      var approved = created.plusDays(10); // duration > 7 days
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    // Add some shorter tasks to mix
    for (int i = 0; i < 30; i++) {
      var created = now.minusDays(5 + i);
      var approved = created.plusDays(3); // duration <= 7 days
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertTrue(service.hasCompletedLongDurationTasks(event));
  }

  @Test
  void hasCompletedLongDurationTasks_shouldReturnFalseWhenFewerThan50LongTasks() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();

    // Add 49 tasks longer than 7 days
    for (int i = 0; i < 49; i++) {
      var created = now.minusDays(20 + i);
      var approved = created.plusDays(10); // duration > 7 days
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }
    // Add some shorter tasks to mix
    for (int i = 0; i < 50; i++) {
      var created = now.minusDays(5 + i);
      var approved = created.plusDays(3); // duration <= 7 days
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasCompletedLongDurationTasks(event));
  }

  @Test
  void hasCompletedLongDurationTasks_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(new ArrayList<>());
    assertFalse(service.hasCompletedLongDurationTasks(event));
  }

  @Test
  void hasCompletedLongDurationTasks_shouldReturnFalseWhenAllTasksShortDuration() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();

    // Add 100 tasks all <= 7 days duration
    for (int i = 0; i < 100; i++) {
      var created = now.minusDays(10 + i);
      var approved = created.plusDays(7); // duration == 7 days, NOT > 7
      tasks.add(createTask(created, approved, approved.plusDays(1), TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasCompletedLongDurationTasks(event));
  }

  @Test
  void hasMaintained90PercentCompletionFor12Months_shouldReturnFalseWhenNoTasks() {
    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(new ArrayList<>());
    assertFalse(service.hasMaintained90PercentCompletionFor12Months(event));
  }

  @Test
  void hasMaintained90PercentCompletionFor12Months_shouldReturnFalseWhenMissingMonth() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // Add tasks for 11 out of 12 months (skip one month)
    for (int i = 0; i < 11; i++) {
      tasks.add(createTask(now.minusMonths(i + 1).plusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId));
    }
    // Skip month 5 (for example)
    // add task for month 6 and earlier months, but skip month 5 specifically
    tasks.add(createTask(now.minusMonths(13).plusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId));

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasMaintained90PercentCompletionFor12Months(event));
  }

  @Test
  void hasMaintained90PercentCompletionFor12Months_shouldReturnFalseWhenLessThan90PercentRecent() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();

    // Total 20 tasks, only 17 are recent (within last 12 months)
    for (int i = 0; i < 17; i++) {
      tasks.add(createTask(now.minusMonths(i + 1).plusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId));
    }
    for (int i = 17; i < 20; i++) {
      tasks.add(createTask(now.minusMonths(13 + i), now, now.plusDays(5), TaskPriority.MEDIUM, teamId)); // older than 12 months
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertFalse(service.hasMaintained90PercentCompletionFor12Months(event));
  }

  @Test
  void hasMaintained90PercentCompletionFor12Months_shouldReturnTrueIf90PercentRecentAndMonthly() {
    var now = LocalDateTime.now();
    List<Task> tasks = new ArrayList<>();
    // 12 tasks (1 per month)
    for (int i = 0; i < 13; i++) {
      tasks.add(createTask(now.minusMonths(i + 1).plusDays(1), now, now.plusDays(5), TaskPriority.MEDIUM, teamId));
    }

    when(taskRepository.findAllCompletedTasksAssignedToUser(any(), any(), any())).thenReturn(tasks);
    assertTrue(service.hasMaintained90PercentCompletionFor12Months(event));
  }
}
