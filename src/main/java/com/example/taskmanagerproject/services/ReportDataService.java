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
   * The returned task metrics may include information such as completed tasks, task completion rate,
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
  Object[] fetchTaskMetrics(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Fetches the role of a specified user within the context of a given team.
   * This can be used to determine the user's responsibilities and impact within the team for reporting purposes.
   *
   * @param user The user whose role is to be fetched.
   * @param team The team the user is a part of.
   *
   * @return A string representing the user's role within the team (for example, "Developer", "Manager").
   */
  String fetchUserRole(User user, Team team);

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
}
