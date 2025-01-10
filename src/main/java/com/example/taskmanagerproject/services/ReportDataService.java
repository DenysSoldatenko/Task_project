package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for fetching report-related data.
 */
public interface ReportDataService {

  /**
   * Fetches the task metrics for a specified user, team, project, and date range.
   * The returned task metrics include information such as completed tasks, task completion rate,
   * and other performance indicators.
   *
   * @param user The user whose task metrics are to be fetched.
   * @param team The team the user belongs to, which provides additional context for the report.
   * @param project The project in which the user worked and for which the task metrics are required.
   * @param startDate The start date of the time period for which the metrics are being retrieved.
   * @param endDate The end date of the time period for which the metrics are being retrieved.
   *
   * @return An array of objects representing the task metrics for the specified user, team, project, and date range.
   *         The array may include various metrics like completed tasks, completion rate, etc.
   */
  Object[] fetchUserTaskMetrics(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Fetches the list of achievements for a user within a specified team and project.
   * Achievements could include completing significant milestones, surpassing goals, or other notable accomplishments.
   *
   * @param user The user whose achievements are to be fetched.
   * @param team The team the user is a part of, providing context to their achievements.
   * @param project The project within which the user has achieved certain milestones.
   *
   * @return A list of achievements that the user has earned in the specified team and project.
   */
  List<Achievement> fetchAchievements(User user, Team team, Project project);

  /**
   * Fetches the task metrics for the top performers within a team during a specific date range.
   * This includes metrics for the highest-performing users, such as completed tasks, XP, and achievements unlocked.
   *
   * @param team The team for which top performers are to be fetched.
   * @param startDate The start date of the period to assess the top performers.
   * @param endDate The end date of the period to assess the top performers.
   *
   * @return A list of Object arrays, where each array contains the task metrics for a top performer,
   *         such as name, image, role, tasks completed, and performance metrics.
   */
  List<Object[]> fetchTopPerformersInTeamMetrics(Team team, LocalDateTime startDate, LocalDateTime endDate);
}
