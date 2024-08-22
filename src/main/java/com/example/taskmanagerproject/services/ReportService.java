package com.example.taskmanagerproject.services;

import java.io.IOException;

/**
 * Service interface for generating various reports in PDF format.
 */
public interface ReportService {

  /**
   * Generates a PDF report for a user based on their involvement in a team and project within a specified date range.
   *
   * @param username   The username of the user for whom the report is being generated.
   * @param teamName   The name of the team the user belongs to.
   * @param projectName The name of the project the report should be associated with.
   * @param startDate  The start date for the report period, formatted as "yyyy-MM-dd".
   * @param endDate    The end date for the report period, formatted as "yyyy-MM-dd".
   * @return A byte array containing the generated PDF report.
   * @throws IOException If there is an error during report generation or file handling.
   */
  byte[] generateUserReport(String username, String teamName, String projectName, String startDate, String endDate) throws IOException;
}
