package com.example.taskmanagerproject.utils.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.reports.ReportData;
import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.services.ReportDataService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class ReportTemplateProcessorTest {

  @Mock
  private ReportDataService reportDataService;

  @Mock
  private TeamUserRepository teamUserRepository;

  @InjectMocks
  private ReportTemplateProcessor processor;

  private User user;
  private Team team;
  private Project project;
  private ReportData reportData;
  private MockedStatic<ReportMetricUtil> metricUtilMock;
  private MockedStatic<ReportTemplateUtil> templateUtilMock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = new User();
    user.setId(1L);
    user.setUsername("user@gmail.com");
    user.setFullName("Test User");

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");
    team.setDescription("Team Description");

    project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setDescription("Project Description");

    reportData = new ReportData(
      user, team, project,
      LocalDateTime.of(2025, 6, 1, 0, 0),
      LocalDateTime.of(2025, 6, 30, 23, 59)
    );

    metricUtilMock = mockStatic(ReportMetricUtil.class);
    templateUtilMock = mockStatic(ReportTemplateUtil.class);
  }

  @AfterEach
  void tearDown() {
    metricUtilMock.close();
    templateUtilMock.close();
  }

  @Test
  void shouldPopulateUserPerformanceTemplate() {
    String template = "{startDate}|{endDate}|{fullName}|{email}|{role}|{teamName}|"
        + "{teamObjective}|{projectName}|{projectDescription}|{tasksCompleted}|{taskCompletionRate}|"
        + "{onTimeTasks}|{averageTaskDuration}|{bugFixesResolved}|{criticalTasksSolved}|{achievements}|"
        + "{taskCompletionProgress}|{bugFixProgress}|{criticalTaskResolution}|{onTimeApprovalRate}|{userLevelName}|"
        + "{performanceStars}";
    Object[] metrics = new Object[]{null, null, "TEAM_LEADER", 100L, 80L, "80%", 60L, 50L, 40L, 30L, 20L, 7200.0};
    List<Achievement> achievements = List.of(new Achievement(101L, "Task Master", "Completed 100 tasks", null));

    when(reportDataService.fetchAchievements(user, team, project)).thenReturn(achievements);
    metricUtilMock.when(() -> ReportMetricUtil.calculatePercentage(80L, 100L)).thenReturn(80.0);
    metricUtilMock.when(() -> ReportMetricUtil.calculatePercentage(40L, 50L)).thenReturn(80.0);
    metricUtilMock.when(() -> ReportMetricUtil.calculatePercentage(60L, 80L)).thenReturn(75.0);
    metricUtilMock.when(() -> ReportMetricUtil.calculatePercentage(20L, 30L)).thenReturn(66.67);
    metricUtilMock.when(() -> ReportMetricUtil.determineUserLevel(80.0, 80.0, 75.0, 66.67)).thenReturn(4);
    metricUtilMock.when(() -> ReportMetricUtil.getUserLevelName(4)).thenReturn("Elite");
    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("TEAM_LEADER")).thenReturn("Team Leader");
    metricUtilMock.when(() -> ReportMetricUtil.formatDuration(7200.0)).thenReturn("120.0 hours, 0.0 minutes");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(80.0)).thenReturn("80.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(75.0)).thenReturn("75.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(66.67)).thenReturn("66.7");
    templateUtilMock.when(() -> ReportTemplateUtil.generateAchievementsHtml(achievements)).thenReturn("<div>Task Master</div>");
    templateUtilMock.when(() -> ReportTemplateUtil.generateStarsHtml(4)).thenReturn("★★★★☆");

    String result = processor.populateUserPerformanceTemplate(template, reportData, metrics);

    String expected = "2025-06-01|2025-06-30|Test User|user@gmail.com|Team Leader|Test Team|"
        + "Team Description|Test Project|Project Description|80/100|80%|60/80|120.0 hours, 0.0 minutes|40/50|20/30|"
        + "<div>Task Master</div>|80.0|80.0|66.7|75.0|Elite|★★★★☆";
    assertEquals(expected, result);
  }

  @Test
  void shouldHandleEmptyAchievementsInUserPerformanceTemplate() {
    String template = "{achievements}";
    Object[] metrics = new Object[]{null, null, "MEMBER", 100L, 80L, "80%", 60L, 50L, 40L, 30L, 20L, 7200.0};

    when(reportDataService.fetchAchievements(user, team, project)).thenReturn(Collections.emptyList());
    templateUtilMock.when(() -> ReportTemplateUtil.generateAchievementsHtml(anyList())).thenReturn("");
    metricUtilMock.when(() -> ReportMetricUtil.calculatePercentage(any(), any())).thenReturn(0.0);
    metricUtilMock.when(() -> ReportMetricUtil.determineUserLevel(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(1);
    templateUtilMock.when(() -> ReportTemplateUtil.generateStarsHtml(1)).thenReturn("★☆☆☆☆");
    metricUtilMock.when(() -> ReportMetricUtil.getUserLevelName(1)).thenReturn("Beginner");
    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("MEMBER")).thenReturn("Member");
    metricUtilMock.when(() -> ReportMetricUtil.formatDuration(anyDouble())).thenReturn("0.0 hours, 0.0 minutes");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(anyDouble())).thenReturn("0.0");

    String result = processor.populateUserPerformanceTemplate(template, reportData, metrics);
    assertEquals("", result);
  }

  @Test
  void shouldThrowNullPointerExceptionForNullMetricsInUserPerformanceTemplate() {
    String template = "{tasksCompleted}";
    assertThrows(NullPointerException.class, () -> processor.populateUserPerformanceTemplate(template, reportData, null));
  }

  @Test
  void shouldPopulateTopPerformersInTeamTemplate() {
    String template = "{startDate}|{endDate}|{team_name}|{top1_name}|{top1_role}|"
        + "{top1_image}|{top1_tasks}|{top1_xp}|{top1_achievements}|{top2_name}|{top2_role}|{top2_image}|{top2_tasks}|"
        + "{top2_xp}|{top2_achievements}";
    List<Object[]> metrics = List.of(
      new Object[]{"User1", "user1.jpg", "TEAM_LEADER", 100L, 80L, new BigDecimal("90.0"), 5L},
      new Object[]{"User2", "user2.jpg", "MEMBER", 50L, 40L, new BigDecimal("85.0"), 3L}
    );

    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("TEAM_LEADER")).thenReturn("Team Leader");
    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("MEMBER")).thenReturn("Member");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(90.0)).thenReturn("90.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(85.0)).thenReturn("85.0");
    templateUtilMock.when(() -> ReportTemplateUtil.generateImageUser("user1.jpg")).thenReturn("<img src='user1.jpg'/>");
    templateUtilMock.when(() -> ReportTemplateUtil.generateImageUser("user2.jpg")).thenReturn("<img src='user2.jpg'/>");
    templateUtilMock.when(() -> ReportTemplateUtil.replacePlaceholders(any(String.class), anyMap())).thenAnswer(invocation -> {
      String t = invocation.getArgument(0);
      Map<String, String> p = invocation.getArgument(1);
      for (Map.Entry<String, String> entry : p.entrySet()) {
        t = t.replace(entry.getKey(), entry.getValue());
      }
      return t;
    });

    String result = processor.populateTopPerformersInTeamTemplate(template, reportData, metrics);

    String expected = "2025-06-01|2025-06-30|Test Team|User1|Team Leader|<img src='user1.jpg'/>|80/100|90.0|5|User2|Member|<img src='user2.jpg'/>|40/50|85.0|3";
    assertEquals(expected, result);
  }

  @Test
  void shouldPopulateTaskProgressTemplate() {
    final List<Object[]> metrics = List.<Object[]>of(new Object[]{"2025-06-01", 10L});

    Role role = new Role();
    role.setName("TEAM_LEADER");

    when(teamUserRepository.findRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("TEAM_LEADER")).thenReturn("Team Leader");
    templateUtilMock.when(() -> ReportTemplateUtil.generateChartHtml(metrics)).thenReturn("<div>Chart</div>");
    templateUtilMock.when(() -> ReportTemplateUtil.replacePlaceholders(any(String.class), anyMap())).thenAnswer(invocation -> {
      String t = invocation.getArgument(0);
      Map<String, String> p = invocation.getArgument(1);
      for (Map.Entry<String, String> entry : p.entrySet()) {
        t = t.replace(entry.getKey(), entry.getValue());
      }
      return t;
    });

    String template = "{startDate}|{endDate}|{teamName}|{projectName}|{fullName}|{email}|{role}|{chart_bars}";
    String result = processor.populateTaskProgressTemplate(template, reportData, metrics);

    String expected = "2025-06-01|2025-06-30|Test Team|Test Project|Test User|user@gmail.com|Team Leader|<div>Chart</div>";
    assertEquals(expected, result);
  }

  @Test
  void shouldThrowNullPointerExceptionForNullRoleInTaskProgressTemplate() {
    String template = "{role}";
    List<Object[]> metrics = List.<Object[]>of(new Object[]{"2025-06-01", 10L});

    when(teamUserRepository.findRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(null);

    assertThrows(NullPointerException.class, () -> processor.populateTaskProgressTemplate(template, reportData, metrics));
  }

  @Test
  void shouldPopulateTeamPerformanceTemplate() {
    String template = "{startDate}|{endDate}|{teamName}|{team_members}";
    List<Object[]> metrics = List.<Object[]>of(new Object[]{"User1", "user1@gmail.com", "TEAM_LEADER", 80L, 100L});

    templateUtilMock.when(() -> ReportTemplateUtil.generateTeamMemberHtml(any(Object[].class))).thenReturn("<div>User1</div>");
    templateUtilMock.when(() -> ReportTemplateUtil.replacePlaceholders(any(String.class), anyMap())).thenAnswer(invocation -> {
      String t = invocation.getArgument(0);
      Map<String, String> p = invocation.getArgument(1);
      for (Map.Entry<String, String> entry : p.entrySet()) {
        t = t.replace(entry.getKey(), entry.getValue());
      }
      return t;
    });

    String result = processor.populateTeamPerformanceTemplate(template, reportData, metrics);

    String expected = "2025-06-01|2025-06-30|Test Team|<div>User1</div>";
    assertEquals(expected, result);
  }

  @Test
  void shouldPopulateProjectPerformanceTemplate() {
    String template = "{startDate}|{endDate}|{projectName}|{project_data}";
    List<Object[]> metrics = List.<Object[]>of(new Object[]{"User1", "user1@gmail.com", 80L, 100L});

    templateUtilMock.when(() -> ReportTemplateUtil.generateProjectMemberHtml(metrics)).thenReturn("<div>Project Data</div>");
    templateUtilMock.when(() -> ReportTemplateUtil.replacePlaceholders(any(String.class), anyMap())).thenAnswer(invocation -> {
      String t = invocation.getArgument(0);
      Map<String, String> p = invocation.getArgument(1);
      for (Map.Entry<String, String> entry : p.entrySet()) {
        t = t.replace(entry.getKey(), entry.getValue());
      }
      return t;
    });

    String result = processor.populateProjectPerformanceTemplate(template, reportData, metrics);

    String expected = "2025-06-01|2025-06-30|Test Project|<div>Project Data</div>";
    assertEquals(expected, result);
  }

  @Test
  void shouldHandleEmptyMetricsInProjectPerformanceTemplate() {
    List<Object[]> metrics = Collections.emptyList();
    templateUtilMock.when(() -> ReportTemplateUtil.generateProjectMemberHtml(metrics)).thenReturn("");

    templateUtilMock.when(() ->
      ReportTemplateUtil.replacePlaceholders(
        eq("{projectName}"),
        eq(Map.of(
          "{startDate}", "2025-06-01",
          "{endDate}", "2025-06-30",
          "{projectName}", "Test Project",
          "{project_data}", ""
        ))
      )
    ).thenReturn("Test Project");

    String result = processor.populateProjectPerformanceTemplate("{projectName}", reportData, metrics);
    assertEquals("Test Project", result);
  }
}