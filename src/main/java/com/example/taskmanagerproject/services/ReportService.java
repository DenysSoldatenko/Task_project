package com.example.taskmanagerproject.services;

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
   */
  byte[] buildUserReport(String username, String teamName, String projectName, String startDate, String endDate);

  /**
   * Generates a PDF report displaying the top performers in a team within a specified date range.
   *
   * @param teamName   The name of the team for which the report is being generated.
   * @param projectName The name of the project the report should be associated with.
   * @param startDate  The start date for the report period, formatted as "yyyy-MM-dd".
   * @param endDate    The end date for the report period, formatted as "yyyy-MM-dd".
   * @return A byte array containing the generated PDF report.
   */
  byte[] buildTopPerformersInTeamReport(String teamName, String projectName, String startDate, String endDate);

  /**
   * Generates a PDF report displaying task progress for a user within a specific team and project in a given date range.
   *
   * @param username   The username of the user for whom the report is being generated.
   * @param teamName   The name of the team the user belongs to.
   * @param projectName The name of the project the report should be associated with.
   * @param startDate  The start date for the report period, formatted as "yyyy-MM-dd".
   * @param endDate    The end date for the report period, formatted as "yyyy-MM-dd".
   * @return A byte array containing the generated PDF report.
   */
  byte[] buildTaskProgressReport(String username, String teamName, String projectName, String startDate, String endDate);

  /**
   * Generates a PDF report displaying the overall performance and progress of a team within a specified date range.
   *
   * @param teamName   The name of the team for which the report is being generated.
   * @param projectName The name of the project the report should be associated with.
   * @param startDate  The start date for the report period, formatted as "yyyy-MM-dd".
   * @param endDate    The end date for the report period, formatted as "yyyy-MM-dd".
   * @return A byte array containing the generated PDF report.
   */
  byte[] buildTeamReport(String teamName, String projectName, String startDate, String endDate);
}
