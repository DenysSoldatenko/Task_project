package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.TASK_METRICS_NOT_FOUND_ERROR;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_PERFORMANCE_NOT_FOUND_ERROR;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.loadTemplate;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.reports.ReportData;
import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.example.taskmanagerproject.services.ReportDataService;
import com.example.taskmanagerproject.services.ReportService;
import com.example.taskmanagerproject.utils.factories.PdfGenerationFactory;
import com.example.taskmanagerproject.utils.reports.ReportTemplateProcessor;
import com.example.taskmanagerproject.utils.validators.ReportValidator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ReportService interface.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private static final String USER_TEMPLATE_PATH = "src/main/resources/report_templates/user_template.html";
  private static final String TOP_PERFORMERS_TEMPLATE_PATH = "src/main/resources/report_templates/top_performers_template.html";
  private static final String TASK_PROGRESS_TEMPLATE_PATH = "src/main/resources/report_templates/task_progress_report.html";

  private final ReportValidator reportValidator;
  private final ReportDataService reportDataService;
  private final ReportTemplateProcessor htmlProcessor;

  @Override
  public byte[] buildUserReport(String username, String teamName, String projectName, String startDate, String endDate) {
    ReportData reportData = reportValidator.validateUserData(username, teamName, projectName, startDate, endDate);
    return generateReport(
      () -> reportDataService.fetchUserTaskMetrics(reportData.user(), reportData.team(), reportData.project(), reportData.startDate(), reportData.endDate()),
      taskMetrics -> htmlProcessor.populateUserTemplate(loadTemplate(USER_TEMPLATE_PATH), reportData.user(), reportData.team(), reportData.project(), reportData.startDate(), reportData.endDate(), taskMetrics),
      format(TASK_METRICS_NOT_FOUND_ERROR, username, projectName, startDate, endDate)
    );
  }

  @Override
  public byte[] buildTopPerformersInTeamReport(String teamName, String startDate, String endDate) {
    ReportData reportData = reportValidator.validateTopPerformersData(teamName, startDate, endDate);
    return generateReport(
      () -> reportDataService.fetchTopPerformersInTeamMetrics(reportData.team(), reportData.startDate(), reportData.endDate()),
      metrics -> htmlProcessor.populateTopPerformersInTeamTemplate(loadTemplate(TOP_PERFORMERS_TEMPLATE_PATH), reportData.team(), reportData.startDate(), reportData.endDate(), metrics),
      format(TEAM_PERFORMANCE_NOT_FOUND_ERROR, teamName, startDate, endDate)
    );
  }

  @Override
  public byte[] buildTaskProgressReport(String username, String teamName, String projectName, String startDate, String endDate) {
    ReportData reportData = reportValidator.validateUserData(username, teamName, projectName, startDate, endDate);
    return generateReport(
      () -> reportDataService.fetchProgressMetrics(reportData.user(), reportData.team(), reportData.project(), reportData.startDate(), reportData.endDate()),
      metrics -> htmlProcessor.populateTaskProgressTemplate(loadTemplate(TASK_PROGRESS_TEMPLATE_PATH), reportData.user(), reportData.team(), reportData.project(), reportData.startDate(), reportData.endDate(), metrics),
      format(TASK_METRICS_NOT_FOUND_ERROR, username, projectName, startDate, endDate)
    );
  }

  /**
   * Generalized method to generate a report.
   *
   * @param dataSupplier  Supplier function to fetch report data.
   * @param htmlPopulator Function to populate the template with data.
   * @param errorMessage  Error message if data is not found.
   * @return Generated PDF as a byte array.
   */
  private <T> byte[] generateReport(Supplier<T> dataSupplier, Function<T, String> htmlPopulator, String errorMessage) {
    return Optional.ofNullable(dataSupplier.get())
      .map(htmlPopulator)
      .map(PdfGenerationFactory::generatePdfFromHtml)
      .orElseThrow(() -> new PdfGenerationException(errorMessage));
  }
}