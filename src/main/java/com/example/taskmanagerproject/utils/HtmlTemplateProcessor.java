package com.example.taskmanagerproject.utils;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.joining;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.services.ReportDataService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
   * @param start the start date of the report
   * @param end the end date of the report
   * @param metrics the array of metrics used to calculate performance and other details
   * @return the populated HTML template as a String
   */
  public String populateUserTemplate(String template, User user, Team team, Project project, LocalDateTime start, LocalDateTime end, Object[] metrics) {
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

    return template.replace("{startDate}", start.format(DATE_FORMATTER))
      .replace("{endDate}", end.format(DATE_FORMATTER))
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
}
