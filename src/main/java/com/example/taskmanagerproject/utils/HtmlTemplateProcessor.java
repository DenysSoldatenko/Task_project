package com.example.taskmanagerproject.utils;

import static java.lang.Double.parseDouble;
import static java.lang.Math.min;
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
    List<Achievement> achievements = reportDataService.fetchAchievements(user, team, project);
    double completionRate = ReportMetricUtil.calculatePercentage(metrics[4], metrics[3]);
    double bugFixRate = ReportMetricUtil.calculatePercentage(metrics[8], metrics[7]);
    double averageTaskDuration = parseDouble(metrics[11].toString());
    double approvalRate = ReportMetricUtil.calculatePercentage(metrics[6], metrics[4]);
    double criticalResolution = ReportMetricUtil.calculatePercentage(metrics[10], metrics[9]);
    int userLevel = ReportMetricUtil.determineUserLevel(completionRate, bugFixRate, approvalRate, criticalResolution);
    String userLevelName = ReportMetricUtil.getUserLevelName(userLevel);
    String performanceStars = generateStarsHtml(userLevel);

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
      .replace("{achievements}", generateAchievementsHtml(achievements))
      .replace("{taskCompletionProgress}", ReportMetricUtil.formatPercentage(completionRate))
      .replace("{bugFixProgress}", ReportMetricUtil.formatPercentage(bugFixRate))
      .replace("{criticalTaskResolution}", ReportMetricUtil.formatPercentage(criticalResolution))
      .replace("{onTimeApprovalRate}", ReportMetricUtil.formatPercentage(approvalRate))
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

    for (int i = 0; i < min(3, metrics.size()); i++) {
      Object[] data = metrics.get(i);
      placeholders.put("{top" + (i + 1) + "_name}", (String) data[0]);
      placeholders.put("{top" + (i + 1) + "_role}", ReportMetricUtil.formatRoleName(data[2]));
      placeholders.put("{top" + (i + 1) + "_image}", generateImageUser((String) data[1]));
      placeholders.put("{top" + (i + 1) + "_tasks}", data[4] + "/" + data[3]);
      placeholders.put("{top" + (i + 1) + "_xp}", ReportMetricUtil.formatPercentage(((BigDecimal) data[5]).doubleValue()));
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
