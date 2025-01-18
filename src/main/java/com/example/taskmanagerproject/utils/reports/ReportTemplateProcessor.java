package com.example.taskmanagerproject.utils.reports;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.joining;

import com.example.taskmanagerproject.dtos.reports.ReportData;
import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.services.ReportDataService;
import java.math.BigDecimal;
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
   * Populates the user performance template with relevant data including tasks completed, task completion rate,
   * bug fixes, critical task resolution, and user achievements.
   *
   * @param template The HTML template to populate with data.
   * @param reportData The report data containing the user and team information.
   * @param metrics The metrics data for the user, including task counts and performance metrics.
   * @return The populated HTML template with replaced placeholders.
   */
  public String populateUserPerformanceTemplate(String template, ReportData reportData, Object[] metrics) {
    List<Achievement> achievements = reportDataService.fetchAchievements(reportData.user(), reportData.team(), reportData.project());
    double completionRate = ReportMetricUtil.calculatePercentage(metrics[4], metrics[3]);
    double bugFixRate = ReportMetricUtil.calculatePercentage(metrics[8], metrics[7]);
    double averageTaskDuration = parseDouble(metrics[11].toString());
    double approvalRate = ReportMetricUtil.calculatePercentage(metrics[6], metrics[4]);
    double criticalResolution = ReportMetricUtil.calculatePercentage(metrics[10], metrics[9]);
    int userLevel = ReportMetricUtil.determineUserLevel(completionRate, bugFixRate, approvalRate, criticalResolution);
    String userLevelName = ReportMetricUtil.getUserLevelName(userLevel);
    String performanceStars = ReportTemplateUtil.generateStarsHtml(userLevel);

    return template.replace("{startDate}", reportData.startDate().format(DATE_FORMATTER))
      .replace("{endDate}", reportData.endDate().format(DATE_FORMATTER))
      .replace("{fullName}", reportData.user().getFullName())
      .replace("{email}", reportData.user().getUsername())
      .replace("{role}", ReportMetricUtil.formatRoleName(metrics[2]))
      .replace("{teamName}", reportData.team().getName())
      .replace("{teamObjective}", reportData.team().getDescription())
      .replace("{projectName}", reportData.project().getName())
      .replace("{projectDescription}", reportData.project().getDescription())
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
   * Populates the template for top performers in the team with data for each member, including name, role,
   * tasks completed, achievements, and experience percentage.
   *
   * @param template The HTML template to populate with data.
   * @param reportData The report data containing the team information.
   * @param metrics The metrics for each team member, including their performance and achievements.
   * @return The populated HTML template with placeholders replaced with actual data.
   */
  public String populateTopPerformersInTeamTemplate(String template, ReportData reportData, List<Object[]> metrics) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("{startDate}", reportData.startDate().format(DATE_FORMATTER));
    placeholders.put("{endDate}", reportData.endDate().format(DATE_FORMATTER));
    placeholders.put("{team_name}", reportData.team().getName());

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
   * Populates the task progress template with data related to the task progress of a user or team,
   * including chart bars and other relevant metrics.
   *
   * @param template The HTML template to populate with data.
   * @param reportData The report data containing the user or team information.
   * @param metrics The task progress data for the user or team.
   * @return The populated HTML template with task progress data inserted.
   */
  public String populateTaskProgressTemplate(String template, ReportData reportData, List<Object[]> metrics) {
    Map<String, String> placeholders = Map.of(
        "{startDate}", reportData.startDate().format(DATE_FORMATTER),
        "{endDate}", reportData.endDate().format(DATE_FORMATTER),
        "{teamName}", reportData.team().getName(),
        "{projectName}", reportData.project().getName(),
        "{fullName}", reportData.user().getFullName(),
        "{email}", reportData.user().getUsername(),
        "{role}", ReportMetricUtil.formatRoleName(reportData.user().getRole().getName()),
        "{chart_bars}", ReportTemplateUtil.generateChartHtml(metrics)
    );

    return ReportTemplateUtil.replacePlaceholders(template, placeholders);
  }

  /**
   * Populates the team performance template with data for the entire team, including member performance metrics
   * and team information.
   *
   * @param template The HTML template to populate with data.
   * @param reportData The report data containing the team information.
   * @param metrics The performance metrics for each team member.
   * @return The populated HTML template with placeholders replaced with actual team and member data.
   */
  public String populateTeamPerformanceTemplate(String template, ReportData reportData, List<Object[]> metrics) {
    Map<String, String> placeholders = Map.of(
        "{startDate}", reportData.startDate().format(DATE_FORMATTER),
        "{endDate}", reportData.endDate().format(DATE_FORMATTER),
        "{teamName}", reportData.team().getName(),
        "{team_members}", metrics.stream().map(ReportTemplateUtil::generateTeamMemberHtml).collect(joining())
    );

    return ReportTemplateUtil.replacePlaceholders(template, placeholders);
  }
}
