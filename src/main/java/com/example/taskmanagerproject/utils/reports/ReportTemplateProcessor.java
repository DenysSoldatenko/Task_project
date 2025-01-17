package com.example.taskmanagerproject.utils.reports;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.joining;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.services.ReportDataService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The ReportTemplateProcessor class is responsible for populating an HTML template with the necessary
 * user, team, project, and metric data to generate a report. It processes the template and replaces
 * placeholders with actual values like user information, metrics, achievements, and performance.
 */
@Component
@RequiredArgsConstructor
public final class ReportTemplateProcessor {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final ReportDataService reportDataService;

  /**
   * Populates an HTML template with dynamic data, such as user, team, project, and performance metrics.
   *
   * @param template  the HTML template with placeholders
   * @param user      the user object containing user-specific data
   * @param team      the team object containing team-specific data
   * @param project   the project object containing project-specific data
   * @param startDate the startDate date of the report
   * @param endDate   the endDate date of the report
   * @param metrics   the array of metrics used to calculate performance and other details
   * @return the populated HTML template as a String
   */
  public String populateUserPerformanceTemplate(String template, User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate, Object[] metrics) {
    List<Achievement> achievements = reportDataService.fetchAchievements(user, team, project);
    double completionRate = ReportMetricUtil.calculatePercentage(metrics[4], metrics[3]);
    double bugFixRate = ReportMetricUtil.calculatePercentage(metrics[8], metrics[7]);
    double averageTaskDuration = parseDouble(metrics[11].toString());
    double approvalRate = ReportMetricUtil.calculatePercentage(metrics[6], metrics[4]);
    double criticalResolution = ReportMetricUtil.calculatePercentage(metrics[10], metrics[9]);
    int userLevel = ReportMetricUtil.determineUserLevel(completionRate, bugFixRate, approvalRate, criticalResolution);
    String userLevelName = ReportMetricUtil.getUserLevelName(userLevel);
    String performanceStars = ReportTemplateUtil.generateStarsHtml(userLevel);

    return template.replace("{startDate}", startDate.format(DATE_FORMATTER))
      .replace("{endDate}", endDate.format(DATE_FORMATTER))
      .replace("{fullName}", user.getFullName())
      .replace("{email}", user.getUsername())
      .replace("{role}", ReportMetricUtil.formatRoleName(metrics[2]))
      .replace("{teamName}", team.getName())
      .replace("{teamObjective}", team.getDescription())
      .replace("{projectName}", project.getName())
      .replace("{projectDescription}", project.getDescription())
      .replace("{tasksCompleted}", metrics[4] + "/" + metrics[3])
      .replace("{taskCompletionRate}", metrics[5].toString())
      .replace("{onTimeTasks}", metrics[6] + "/" + metrics[4])
      .replace("{averageTaskDuration}", ReportMetricUtil.formatDuration(averageTaskDuration))
      .replace("{bugFixesResolved}", metrics[8] + "/" + metrics[7])
      .replace("{criticalTasksSolved}", metrics[10] + "/" + metrics[9])
      .replace("{achievements}", ReportTemplateUtil.generateAchievementsHtml(achievements))
      .replace("{taskCompletionProgress}", ReportMetricUtil.formatPercentage(completionRate))
      .replace("{bugFixProgress}", ReportMetricUtil.formatPercentage(bugFixRate))
      .replace("{criticalTaskResolution}", ReportMetricUtil.formatPercentage(criticalResolution))
      .replace("{onTimeApprovalRate}", ReportMetricUtil.formatPercentage(approvalRate))
      .replace("{userLevelName}", userLevelName)
      .replace("{performanceStars}", performanceStars);
  }

  /**
   * Populates a given template with data for the top performers in a team during a specified date range.
   * This method replaces placeholders in the template with the actual data of the top performers,
   * such as their name, role, image, tasks completed, XP percentage, and achievements.
   *
   * @param template  The HTML template that contains placeholders to be replaced.
   * @param team      The team for which the top performers' data is being populated.
   * @param startDate The start date of the time period for which the report is generated.
   * @param endDate   The end date of the time period for which the report is generated.
   * @param metrics   A list of top performers' metrics.
   * @return The populated HTML template as a string with placeholders replaced by actual data.
   */
  public String populateTopPerformersInTeamTemplate(String template, Team team, LocalDateTime startDate, LocalDateTime endDate, List<Object[]> metrics) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("{startDate}", startDate.format(DATE_FORMATTER));
    placeholders.put("{endDate}", endDate.format(DATE_FORMATTER));
    placeholders.put("{team_name}", team.getName());

    for (int i = 0; i < metrics.size(); i++) {
      Object[] data = metrics.get(i);
      placeholders.put("{top" + (i + 1) + "_name}", (String) data[0]);
      placeholders.put("{top" + (i + 1) + "_role}", ReportMetricUtil.formatRoleName(data[2]));
      placeholders.put("{top" + (i + 1) + "_image}", ReportTemplateUtil.generateImageUser((String) data[1]));
      placeholders.put("{top" + (i + 1) + "_tasks}", data[4] + "/" + data[3]);
      placeholders.put("{top" + (i + 1) + "_xp}", ReportMetricUtil.formatPercentage(((BigDecimal) data[5]).doubleValue()));
      placeholders.put("{top" + (i + 1) + "_achievements}", String.valueOf(data[6]));
    }

    return ReportTemplateUtil.replacePlaceholders(template, placeholders);
  }

  /**
   * Populates a given template with data related to a user's task progress within a team and project
   * during a specified date range. This method replaces placeholders in the template with actual data
   * such as the user's name, email, role, team and project names, as well as a chart representation of
   * task progress metrics.
   *
   * @param template  The HTML template that contains placeholders to be replaced.
   * @param user      The user whose task progress data is being populated.
   * @param team      The team to which the user belongs.
   * @param project   The project within the team to which the task progress data pertains.
   * @param startDate The start date of the time period for which the report is generated.
   * @param endDate   The end date of the time period for which the report is generated.
   * @param metrics   A list of task progress metrics for the user, which will be used to populate the chart and task-related data.
   * @return The populated HTML template as a string, with placeholders replaced by actual task progress data.
   */
  public String populateTaskProgressTemplate(String template, User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate, List<Object[]> metrics) {
    Map<String, String> placeholders = Map.of(
        "{startDate}", startDate.format(DATE_FORMATTER),
        "{endDate}", endDate.format(DATE_FORMATTER),
        "{teamName}", team.getName(),
        "{projectName}", project.getName(),
        "{fullName}", user.getFullName(),
        "{email}", user.getUsername(),
        "{role}", ReportMetricUtil.formatRoleName(user.getRole().getName()),
        "{chart_bars}", ReportTemplateUtil.generateChartHtml(metrics)
    );

    return ReportTemplateUtil.replacePlaceholders(template, placeholders);
  }

  /**
   * Populates the team performance report template with dynamic data.
   *
   * @param template  The HTML template as a string.
   * @param team      The team whose performance is being reported.
   * @param startDate The start date of the reporting period.
   * @param endDate   The end date of the reporting period.
   * @param metrics   The performance metrics for team members.
   * @return The populated HTML report as a string.
   */
  public String populateTeamPerformanceTemplate(String template, Team team, LocalDateTime startDate,
                                                LocalDateTime endDate, List<Object[]> metrics) {
    Map<String, String> placeholders = Map.of(
        "{startDate}", startDate.format(DATE_FORMATTER),
        "{endDate}", endDate.format(DATE_FORMATTER),
        "{teamName}", team.getName(),
        "{team_members}", metrics.stream().map(ReportTemplateUtil::generateTeamMemberHtml).collect(joining())
    );

    return ReportTemplateUtil.replacePlaceholders(template, placeholders);
  }
}
