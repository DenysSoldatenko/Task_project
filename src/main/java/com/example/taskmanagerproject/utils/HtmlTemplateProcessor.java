package com.example.taskmanagerproject.utils;

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
 * The HtmlTemplateProcessor class is responsible for populating an HTML template with the necessary
 * user, team, project, and metric data to generate a report. It processes the template and replaces
 * placeholders with actual values like user information, metrics, achievements, and performance.
 */
@Component
@RequiredArgsConstructor
public final class HtmlTemplateProcessor {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final ReportDataService reportDataService;

  /**
   * Populates an HTML template with dynamic data, such as user, team, project, and performance metrics.
   *
   * @param template the HTML template with placeholders
   * @param user the user object containing user-specific data
   * @param team the team object containing team-specific data
   * @param project the project object containing project-specific data
   * @param startDate the startDate date of the report
   * @param endDate the endDate date of the report
   * @param metrics the array of metrics used to calculate performance and other details
   * @return the populated HTML template as a String
   */
  public String populateUserTemplate(String template, User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate, Object[] metrics) {
    String role = reportDataService.fetchUserRole(user, team);
    List<Achievement> achievements = reportDataService.fetchAchievements(user, team, project);

    double completionRate = ReportMetricsCalculator.calculatePercentage(metrics[3], metrics[2]);
    double bugFixRate = ReportMetricsCalculator.calculatePercentage(metrics[7], metrics[6]);
    double averageTaskDuration = parseDouble(metrics[10].toString());
    double approvalRate = ReportMetricsCalculator.calculatePercentage(metrics[5], metrics[3]);
    double criticalResolution = ReportMetricsCalculator.calculatePercentage(metrics[9], metrics[8]);
    int userLevel = ReportMetricsCalculator.determineUserLevel(completionRate, bugFixRate, approvalRate, criticalResolution);
    String userLevelName = ReportMetricsCalculator.getUserLevelName(userLevel);
    String performanceStars = generateStarsHtml(userLevel);

    return template.replace("{startDate}", startDate.format(DATE_FORMATTER))
      .replace("{endDate}", endDate.format(DATE_FORMATTER))
      .replace("{fullName}", user.getFullName())
      .replace("{email}", user.getUsername())
      .replace("{role}", role)
      .replace("{teamName}", team.getName())
      .replace("{teamObjective}", team.getDescription())
      .replace("{projectName}", project.getName())
      .replace("{projectDescription}", project.getDescription())
      .replace("{tasksCompleted}", metrics[3] + "/" + metrics[2])
      .replace("{taskCompletionRate}", metrics[4].toString())
      .replace("{onTimeTasks}", metrics[5] + "/" + metrics[3])
      .replace("{averageTaskDuration}", ReportMetricsCalculator.formatDuration(averageTaskDuration))
      .replace("{bugFixesResolved}", metrics[7] + "/" + metrics[6])
      .replace("{criticalTasksSolved}", metrics[9] + "/" + metrics[8])
      .replace("{achievements}", generateAchievementsHtml(achievements))
      .replace("{taskCompletionProgress}", ReportMetricsCalculator.formatPercentage(completionRate))
      .replace("{bugFixProgress}", ReportMetricsCalculator.formatPercentage(bugFixRate))
      .replace("{criticalTaskResolution}", ReportMetricsCalculator.formatPercentage(criticalResolution))
      .replace("{onTimeApprovalRate}", ReportMetricsCalculator.formatPercentage(approvalRate))
      .replace("{userLevelName}", userLevelName)
      .replace("{performanceStars}", performanceStars);
  }

  private String generateStarsHtml(int rating) {
    String filledStar = "<img src=\"https://img.icons8.com/?id=7856\" alt=\"Filled Star\" class=\"star-icon\"/>";
    String emptyStar = "<img src=\"https://img.icons8.com/ios/452/star.png\" alt=\"Empty Star\" class=\"star-icon\"/>";
    return filledStar.repeat(rating) + emptyStar.repeat(5 - rating);
  }

  private String generateAchievementsHtml(List<Achievement> achievements) {
    if (achievements.isEmpty()) {
      return "<div class=\"no-achievements\">There are no achievements yet.</div>";
    }

    return achievements.stream()
      .map(a -> "<div class=\"achievement\">"
        + "<img src=\"" + a.getImageUrl() + "\" alt=\"Achievement Icon\"/>"
        + "<div>"
        + "<div class=\"achievement-title\">" + a.getTitle() + "</div>"
        + "<div class=\"achievement-description\">" + a.getDescription() + "</div>"
        + "</div>"
        + "</div>")
      .collect(joining());
  }

  /**
   * Populates a given template with data for the top performers in a team during a specified date range.
   * This method replaces placeholders in the template with the actual data of the top performers,
   * such as their name, role, image, tasks completed, XP percentage, and achievements.
   *
   * @param template The HTML template that contains placeholders to be replaced.
   * @param team The team for which the top performers' data is being populated.
   * @param startDate The start date of the time period for which the report is generated.
   * @param endDate The end date of the time period for which the report is generated.
   * @param metrics A list of top performers' metrics.
   * @return The populated HTML template as a string with placeholders replaced by actual data.
   */
  public String populateTopPerformersInTeamTemplate(String template, Team team, LocalDateTime startDate, LocalDateTime endDate, List<Object[]> metrics) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("{startDate}", startDate.format(DATE_FORMATTER));
    placeholders.put("{endDate}", endDate.format(DATE_FORMATTER));
    placeholders.put("{team_name}", team.getName());

    for (int i = 0; i < Math.min(3, metrics.size()); i++) {
      Object[] data = metrics.get(i);
      placeholders.put("{top" + (i + 1) + "_name}", (String) data[0]);
      placeholders.put("{top" + (i + 1) + "_role}", (String) data[2]);
      placeholders.put("{top" + (i + 1) + "_image}", generateImageUser((String) data[1]));
      placeholders.put("{top" + (i + 1) + "_tasks}", data[4] + "/" + data[3]);
      placeholders.put("{top" + (i + 1) + "_xp}", ReportMetricsCalculator.formatPercentage(((BigDecimal) data[5]).doubleValue()));
      placeholders.put("{top" + (i + 1) + "_achievements}", String.valueOf(data[6]));
    }

    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue());
    }

    return template;
  }

  private String generateImageUser(String userImageUrl) {
    return "<img src=\"" + (userImageUrl.contains("dummyimage.com") ? userImageUrl : "http://127.0.0.1:9000/images/" + userImageUrl) + "\" alt=\"User Image\" class=\"user-img\"><br/>\n";
  }
}
