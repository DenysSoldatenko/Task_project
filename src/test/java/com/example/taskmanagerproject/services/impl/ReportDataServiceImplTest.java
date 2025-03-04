package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.AchievementRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReportDataServiceImplTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private AchievementRepository achievementRepository;

  @InjectMocks
  private ReportDataServiceImpl reportDataService;

  private User user;
  private Team team;
  private Project project;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Achievement achievement;
  private Object[] metrics;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    user = mock(User.class);
    team = mock(Team.class);
    project = mock(Project.class);
    achievement = mock(Achievement.class);
    metrics = new Object[]{1L, "data"};
    startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
    endDate = LocalDateTime.of(2025, 1, 31, 23, 59);
    when(user.getId()).thenReturn(1L);
    when(team.getId()).thenReturn(1L);
    when(team.getName()).thenReturn("TestTeam");
    when(project.getId()).thenReturn(1L);
    when(project.getName()).thenReturn("TestProject");
  }

  @Test
  void fetchUserPerformanceMetrics_shouldReturnMetricsWhenDataExists() {
    when(taskRepository.getTaskMetricsByAssignedUser(1L, startDate, endDate, "TestProject", "TestTeam")).thenReturn(Collections.singletonList(metrics));
    Object[] result = reportDataService.fetchUserPerformanceMetrics(user, team, project, startDate, endDate);
    assertNotNull(result);
    assertArrayEquals(metrics, result);
    verify(taskRepository).getTaskMetricsByAssignedUser(1L, startDate, endDate, "TestProject", "TestTeam");
  }

  @Test
  void fetchUserPerformanceMetrics_shouldThrowExceptionWhenRepositoryFails() {
    when(taskRepository.getTaskMetricsByAssignedUser(1L, startDate, endDate, "TestProject", "TestTeam")).thenThrow(new RuntimeException("DB failure"));
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> reportDataService.fetchUserPerformanceMetrics(user, team, project, startDate, endDate));
    assertEquals("DB failure", thrown.getMessage());
  }

  @Test
  void fetchAchievements_shouldReturnAchievementsWhenExist() {
    when(achievementRepository.findAchievementsByUserTeamAndProject(1L, 1L, 1L)).thenReturn(List.of(achievement));
    List<Achievement> result = reportDataService.fetchAchievements(user, team, project);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(achievement, result.get(0));
    verify(achievementRepository).findAchievementsByUserTeamAndProject(1L, 1L, 1L);
  }

  @Test
  void fetchAchievements_shouldReturnEmptyListForNegativeIds() {
    when(user.getId()).thenReturn(-1L);
    when(team.getId()).thenReturn(-5L);
    when(project.getId()).thenReturn(-10L);
    when(achievementRepository.findAchievementsByUserTeamAndProject(-1L, -5L, -10L)).thenReturn(Collections.emptyList());
    List<Achievement> result = reportDataService.fetchAchievements(user, team, project);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(achievementRepository).findAchievementsByUserTeamAndProject(-1L, -5L, -10L);
  }

  @Test
  void fetchAchievements_shouldReturnEmptyListWhenNoAchievements() {
    when(achievementRepository.findAchievementsByUserTeamAndProject(1L, 1L, 1L)).thenReturn(Collections.emptyList());
    List<Achievement> result = reportDataService.fetchAchievements(user, team, project);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(achievementRepository).findAchievementsByUserTeamAndProject(1L, 1L, 1L);
  }

  @Test
  void fetchAchievements_shouldHandleZeroIds() {
    when(user.getId()).thenReturn(0L);
    when(team.getId()).thenReturn(0L);
    when(project.getId()).thenReturn(0L);
    when(achievementRepository.findAchievementsByUserTeamAndProject(0L, 0L, 0L)).thenReturn(Collections.emptyList());
    List<Achievement> result = reportDataService.fetchAchievements(user, team, project);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(achievementRepository).findAchievementsByUserTeamAndProject(0L, 0L, 0L);
  }

  @Test
  void fetchTopPerformersInTeamMetrics_shouldReturnMetricsWhenDataExists() {
    when(taskRepository.getTopPerformerMetricsByTeamName("TestTeam", "TestProject", startDate, endDate)).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchTopPerformersInTeamMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getTopPerformerMetricsByTeamName("TestTeam", "TestProject", startDate, endDate);
  }

  @Test
  void fetchTopPerformersInTeamMetrics_shouldReturnEmptyListWhenNoData() {
    when(taskRepository.getTopPerformerMetricsByTeamName("TestTeam", "TestProject", startDate, endDate)).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchTopPerformersInTeamMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getTopPerformerMetricsByTeamName("TestTeam", "TestProject", startDate, endDate);
  }

  @Test
  void fetchProgressMetrics_shouldReturnDailyRatesWhenPeriodIsShort() {
    LocalDateTime shortEndDate = startDate.plusDays(30);
    when(taskRepository.getDailyCompletionRates(startDate, shortEndDate, 1L, "TestProject", "TestTeam")).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchProgressMetrics(user, team, project, startDate, shortEndDate);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getDailyCompletionRates(startDate, shortEndDate, 1L, "TestProject", "TestTeam");
  }

  @Test
  void fetchProgressMetrics_shouldReturnMonthlyRatesWhenPeriodIsLong() {
    LocalDateTime longEndDate = startDate.plusDays(32);
    when(taskRepository.getMonthlyCompletionRates(startDate, longEndDate, 1L, "TestProject", "TestTeam")).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchProgressMetrics(user, team, project, startDate, longEndDate);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getMonthlyCompletionRates(startDate, longEndDate, 1L, "TestProject", "TestTeam");
  }

  @Test
  void fetchProgressMetrics_shouldReturnEmptyListWhenNoData() {
    when(taskRepository.getDailyCompletionRates(startDate, endDate, 1L, "TestProject", "TestTeam")).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchProgressMetrics(user, team, project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getDailyCompletionRates(startDate, endDate, 1L, "TestProject", "TestTeam");
  }

  @Test
  void fetchTeamPerformanceMetrics_shouldReturnMetricsWhenDataExists() {
    when(taskRepository.getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate)).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchTeamPerformanceMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate);
  }

  @Test
  void fetchTeamPerformanceMetrics_shouldReturnEmptyListWhenNoData() {
    when(taskRepository.getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate)).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchTeamPerformanceMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate);
  }

  @Test
  void fetchTeamPerformanceMetrics_shouldReturnEmptyListWhenNoData_verifyNoMoreInteractions() {
    when(taskRepository.getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate)).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchTeamPerformanceMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getAllTeamMemberMetricsByTeamName("TestTeam", "TestProject", startDate, endDate);
    verifyNoMoreInteractions(taskRepository);
  }

  @Test
  void fetchTeamPerformanceMetrics_shouldHandleEmptyProjectOrTeamNames() {
    when(team.getName()).thenReturn("");
    when(project.getName()).thenReturn(" ");
    when(taskRepository.getAllTeamMemberMetricsByTeamName("", " ", startDate, endDate)).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchTeamPerformanceMetrics(team, project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getAllTeamMemberMetricsByTeamName("", " ", startDate, endDate);
  }

  @Test
  void fetchProjectPerformanceMetrics_shouldReturnMetricsWhenDataExists() {
    when(taskRepository.getProjectMetricsByProjectName("TestProject", startDate, endDate)).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchProjectPerformanceMetrics(project, startDate, endDate);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getProjectMetricsByProjectName("TestProject", startDate, endDate);
  }

  @Test
  void fetchProjectPerformanceMetrics_shouldReturnEmptyListWhenNoData() {
    when(taskRepository.getProjectMetricsByProjectName("TestProject", startDate, endDate)).thenReturn(Collections.emptyList());
    List<Object[]> result = reportDataService.fetchProjectPerformanceMetrics(project, startDate, endDate);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(taskRepository).getProjectMetricsByProjectName("TestProject", startDate, endDate);
  }

  @Test
  void fetchProgressMetrics_shouldReturnEmptyWhenStartDateAfterEndDate() {
    LocalDateTime invalidStart = LocalDateTime.of(2025, 2, 1, 0, 0);
    LocalDateTime invalidEnd = LocalDateTime.of(2025, 1, 1, 0, 0);
    List<Object[]> result = reportDataService.fetchProgressMetrics(user, team, project, invalidStart, invalidEnd);
    assertNotNull(result);
    assertTrue(result.isEmpty(), "Expected empty list when startDate is after endDate");
  }

  @Test
  void fetchProjectPerformanceMetrics_shouldHandleSingleInstantDateRange() {
    LocalDateTime instant = LocalDateTime.of(2025, 1, 15, 12, 0);
    when(taskRepository.getProjectMetricsByProjectName("TestProject", instant, instant)).thenReturn(Collections.singletonList(metrics));
    List<Object[]> result = reportDataService.fetchProjectPerformanceMetrics(project, instant, instant);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertArrayEquals(metrics, result.get(0));
    verify(taskRepository).getProjectMetricsByProjectName("TestProject", instant, instant);
  }
}