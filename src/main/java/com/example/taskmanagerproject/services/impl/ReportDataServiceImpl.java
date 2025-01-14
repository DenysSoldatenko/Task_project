package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.AchievementRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.services.ReportDataService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ReportDataService interface.
 */
@Service
@RequiredArgsConstructor
public class ReportDataServiceImpl implements ReportDataService {

  private final TaskRepository taskRepository;
  private final AchievementRepository achievementRepository;

  @Override
  public Object[] fetchUserTaskMetrics(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> taskMetricsList = taskRepository.getTaskMetricsByAssignedUser(user.getId(), startDate, endDate, project.getName(), team.getName());
    return taskMetricsList.get(0);
  }

  @Override
  public List<Achievement> fetchAchievements(User user, Team team, Project project) {
    return achievementRepository.findAchievementsByUserTeamAndProject(user.getId(), team.getId(), project.getId());
  }

  @Override
  public List<Object[]> fetchTopPerformersInTeamMetrics(Team team, LocalDateTime startDate, LocalDateTime endDate) {
    return taskRepository.getTopPerformerMetricsByTeamName(team.getName(), startDate, endDate);
  }

  @Override
  public List<Object[]> fetchProgressMetrics(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    long daysDifference = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());

    if (daysDifference > 31) {
      return taskRepository.getMonthlyCompletionRates(startDate, endDate, user.getId(), project.getName(), team.getName());
    } else {
      return taskRepository.getDailyCompletionRates(startDate, endDate, user.getId(), project.getName(), team.getName());
    }
  }
}
