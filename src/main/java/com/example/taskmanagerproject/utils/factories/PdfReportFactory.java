package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.TASK_METRICS_NOT_FOUND_ERROR;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_PERFORMANCE_NOT_FOUND_ERROR;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.generatePdfFromHtml;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.loadTemplate;
import static java.lang.String.format;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.example.taskmanagerproject.services.ReportDataService;
import com.example.taskmanagerproject.utils.HtmlTemplateProcessor;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Generates PDF reports by integrating data, calculations, and template processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class PdfReportFactory {

  private static final String USER_TEMPLATE_PATH = "src/main/resources/report_templates/user_template.html";
  private static final String TOP_PERFORMERS_TEMPLATE_PATH = "src/main/resources/report_templates/top_performers_template.html";

  private final ReportDataService reportDataService;
  private final HtmlTemplateProcessor htmlProcessor;

  /**
   * Generates a performance report for a user in the context of a team and project.
   * The report is created by fetching task metrics for the specified user, team, and project
   * within the provided date range, and populating a predefined HTML template with that data.
   * Finally, the populated HTML is converted into a PDF format.
   *
   * @param user The user for whom the report is being generated.
   * @param team The team that the user belongs to, providing context for the report.
   * @param project The project for which the performance report is generated.
   * @param startDate The start date of the time period to be covered by the report.
   * @param endDate The end date of the time period to be covered by the report.
   *
   * @return A byte array representing the generated PDF report.
   *
   * @throws PdfGenerationException If no task metrics are found for the user in the specified
   *                                project and date range, or if there are any issues during
   *                                the PDF generation process.
   */
  public byte[] generateUserReport(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    String htmlTemplate = loadTemplate(USER_TEMPLATE_PATH);

    Object[] taskMetrics = reportDataService.fetchUserTaskMetrics(user, team, project, startDate, endDate);
    if (taskMetrics == null) {
      throw new PdfGenerationException(format(TASK_METRICS_NOT_FOUND_ERROR, user.getUsername(), project.getName(), startDate, endDate));
    }

    String populatedHtml = htmlProcessor.populateUserTemplate(htmlTemplate, user, team, project, startDate, endDate, taskMetrics);
    return generatePdfFromHtml(populatedHtml);
  }

  /**
   * Generates a PDF report for top performers in a team within a specified date range.
   * The report is created by fetching performance metrics for the specified team within
   * the provided date range, and populating a predefined HTML template with that data.
   * Finally, the populated HTML is converted into a PDF format.
   *
   * @param team The team for which the top performers report is generated.
   * @param startDate The start date of the time period to be covered by the report.
   * @param endDate The end date of the time period to be covered by the report.
   * @return A byte array representing the generated PDF report.
   */
  public byte[] generateTopPerformersInTeamReport(Team team, LocalDateTime startDate, LocalDateTime endDate) {
    String htmlTemplate = loadTemplate(TOP_PERFORMERS_TEMPLATE_PATH);

    List<Object[]> performersMetrics = reportDataService.fetchTopPerformersInTeamMetrics(team, startDate, endDate);
    if (performersMetrics == null) {
      throw new PdfGenerationException(format(TEAM_PERFORMANCE_NOT_FOUND_ERROR, team.getName(), startDate, endDate));
    }

    String populatedHtml = htmlProcessor.populateTopPerformersInTeamTemplate(htmlTemplate, team, startDate, endDate, performersMetrics);
    return generatePdfFromHtml(populatedHtml);
  }
}
