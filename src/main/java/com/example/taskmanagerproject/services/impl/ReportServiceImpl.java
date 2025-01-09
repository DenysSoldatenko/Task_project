package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.dtos.reports.ReportData;
import com.example.taskmanagerproject.services.ReportService;
import com.example.taskmanagerproject.utils.factories.PdfReportFactory;
import com.example.taskmanagerproject.utils.validators.ReportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the ReportService interface.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final ReportValidator reportValidator;
  private final PdfReportFactory pdfReportFactory;

  @Override
  public byte[] buildUserReport(String username, String teamName, String projectName, String startDate, String endDate) {
    ReportData reportData = reportValidator.validateUserReportData(username, teamName, projectName, startDate, endDate);
    return pdfReportFactory.generateUserReport(
      reportData.user(),
      reportData.team(),
      reportData.project(),
      reportData.startDate(),
      reportData.endDate()
    );
  }

  @Override
  public byte[] buildTopPerformersInTeamReport(String teamName, String startDate, String endDate) {
    ReportData reportData = reportValidator.validateTopPerformersReportData(teamName, startDate, endDate);
    return pdfReportFactory.generateTopPerformersInTeamReport(
      reportData.team(),
      reportData.startDate(),
      reportData.endDate()
    );
  }
}
