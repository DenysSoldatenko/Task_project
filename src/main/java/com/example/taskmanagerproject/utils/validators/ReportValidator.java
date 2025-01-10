package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtil.DATE_RANGE_REQUIRED;
import static com.example.taskmanagerproject.utils.MessageUtil.INVALID_DATE_RANGE;
import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_IN_PROJECT;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_IN_TEAM;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates report-related inputs, including user-team, team-project, and date constraints.
 */
@Component
@RequiredArgsConstructor
public final class ReportValidator {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;
  private final TeamUserRepository teamUserRepository;
  private final ProjectTeamRepository projectTeamRepository;

  /**
   * Validates report parameters and retrieves required entities and parsed dates.
   *
   * @param username    The requesting user's username.
   * @param teamName    The name of the team.
   * @param projectName The name of the project.
   * @param startDate   The report's start date (yyyy-MM-dd).
   * @param endDate     The report's end date (yyyy-MM-dd).
   * @return A `ReportData` object with validated user, team, project, and timestamps.
   */
  public ReportData validateUserReportData(String username, String teamName, String projectName, String startDate, String endDate) {
    User user = getUser(username);
    Team team = getTeam(teamName);
    Project project = getProject(projectName);

    checkUserInTeam(user, team);
    checkTeamInProject(team, project);
    validateDates(startDate, endDate);

    return new ReportData(user, team, project, toStartOfDay(startDate), toEndOfDay(endDate));
  }

  /**
   * Validates report parameters and retrieves required entities and parsed dates for top performers report.
   *
   * @param teamName  The name of the team.
   * @param startDate The report's start date (yyyy-MM-dd).
   * @param endDate   The report's end date (yyyy-MM-dd).
   * @return A `ReportData` object with a validated team and timestamps.
   */
  public ReportData validateTopPerformersReportData(String teamName, String startDate, String endDate) {
    Team team = getTeam(teamName);
    validateDates(startDate, endDate);

    return new ReportData(null, team, null, toStartOfDay(startDate), toEndOfDay(endDate));
  }

  private User getUser(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  private Team getTeam(String teamName) {
    return teamRepository.findByName(teamName)
      .orElseThrow(() -> new ValidationException(TEAM_NOT_FOUND_WITH_NAME + teamName));
  }

  private Project getProject(String projectName) {
    return projectRepository.findByName(projectName)
      .orElseThrow(() -> new ValidationException(PROJECT_NOT_FOUND_WITH_NAME + projectName));
  }

  private void checkUserInTeam(User user, Team team) {
    if (!teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())) {
      throw new ValidationException(USER_NOT_IN_TEAM);
    }
  }

  private void checkTeamInProject(Team team, Project project) {
    if (!projectTeamRepository.existsByProjectNameAndTeamName(project.getName(), team.getName())) {
      throw new ValidationException(TEAM_NOT_IN_PROJECT);
    }
  }

  private void validateDates(String startDate, String endDate) {
    if (startDate == null || endDate == null) {
      throw new ValidationException(DATE_RANGE_REQUIRED);
    }

    LocalDate start = parseDate(startDate);
    LocalDate end = parseDate(endDate);

    if (start.isAfter(end)) {
      throw new ValidationException(INVALID_DATE_RANGE);
    }
  }

  private LocalDate parseDate(String date) {
    return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  private LocalDateTime toStartOfDay(String date) {
    return parseDate(date).atStartOfDay();
  }

  private LocalDateTime toEndOfDay(String date) {
    return parseDate(date).atTime(23, 59, 59);
  }
}
