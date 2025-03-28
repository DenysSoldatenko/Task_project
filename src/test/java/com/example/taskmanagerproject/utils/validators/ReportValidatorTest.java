package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtil.DATE_RANGE_REQUIRED;
import static com.example.taskmanagerproject.utils.MessageUtil.INVALID_DATE_RANGE;
import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_IN_PROJECT;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_IN_TEAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.reports.ReportData;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.ProjectTeamRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportValidatorTest {

  private UserRepository userRepository;
  private TeamRepository teamRepository;
  private ProjectRepository projectRepository;
  private TeamUserRepository teamUserRepository;
  private ProjectTeamRepository projectTeamRepository;

  private ReportValidator reportValidator;

  private final String username = "testuser@gmail.com";
  private final String teamName = "DevTeam";
  private final String projectName = "ProjectX";
  private final String startDate = "2024-01-01";
  private final String endDate = "2024-01-31";

  private User user;
  private Team team;
  private Project project;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    teamRepository = mock(TeamRepository.class);
    projectRepository = mock(ProjectRepository.class);
    teamUserRepository = mock(TeamUserRepository.class);
    projectTeamRepository = mock(ProjectTeamRepository.class);

    reportValidator = new ReportValidator(
      userRepository, teamRepository, projectRepository, teamUserRepository, projectTeamRepository
    );

    user = new User();
    user.setId(1L);
    user.setUsername(username);

    team = new Team();
    team.setId(1L);
    team.setName(teamName);

    project = new Project();
    project.setName(projectName);

    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    when(teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(true);
    when(projectTeamRepository.existsByProjectNameAndTeamName(project.getName(), team.getName())).thenReturn(true);
  }

  @Test
  void validateUserData_shouldReturnValidReportData() {
    ReportData data = reportValidator.validateUserData(username, teamName, projectName, startDate, endDate);
    assertNotNull(data);
    assertEquals(user, data.user());
    assertEquals(team, data.team());
    assertEquals(project, data.project());
    assertTrue(data.startDate().isBefore(data.endDate()));
  }

  @Test
  void validateUserData_shouldThrowWhenUserNotFound() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateUserData("ghost", teamName, projectName, startDate, endDate));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "ghost", ex.getMessage());
  }

  @Test
  void validateTeamData_shouldReturnValidReportData() {
    ReportData data = reportValidator.validateTeamData(teamName, projectName, startDate, endDate);
    assertNotNull(data);
    assertEquals(team, data.team());
    assertEquals(project, data.project());
    assertNull(data.user());
  }

  @Test
  void validateProjectData_shouldReturnValidReportData() {
    ReportData data = reportValidator.validateProjectData(projectName, startDate, endDate);
    assertNotNull(data);
    assertEquals(project, data.project());
    assertNull(data.team());
    assertNull(data.user());
  }

  @Test
  void validateUserData_shouldThrowWhenUserNotInTeam() {
    when(teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(false);
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateUserData(username, teamName, projectName, startDate, endDate));
    assertEquals(USER_NOT_IN_TEAM, ex.getMessage());
  }

  @Test
  void validateUserData_shouldThrowWhenTeamNotInProject() {
    when(projectTeamRepository.existsByProjectNameAndTeamName(project.getName(), team.getName())).thenReturn(false);
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateUserData(username, teamName, projectName, startDate, endDate));
    assertEquals(TEAM_NOT_IN_PROJECT, ex.getMessage());
  }

  @Test
  void validateProjectData_shouldThrowWhenDateRangeIsNull() {
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateProjectData(projectName, null, null));
    assertEquals(DATE_RANGE_REQUIRED, ex.getMessage());
  }

  @Test
  void validateTeamData_shouldThrowWhenStartDateAfterEndDate() {
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateTeamData(teamName, projectName, "2024-03-01", "2024-01-01"));
    assertEquals(INVALID_DATE_RANGE, ex.getMessage());
  }

  @Test
  void validateProjectData_shouldThrowWhenProjectNotFound() {
    when(projectRepository.findByName("Unknown")).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> reportValidator.validateProjectData("Unknown", startDate, endDate));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + "Unknown", ex.getMessage());
  }
}
