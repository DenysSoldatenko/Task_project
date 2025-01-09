package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling report-related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/reports")
@Tag(name = "Report Controller", description = "Endpoints for generating reports in PDF format")
public class ReportController {

  private final ReportService reportService;

  /**
   * Generates a PDF report containing the user's task details and performance
   * within a specified team, project, and date range.
   *
   * @param username    The email address of the user for whom the report is generated.
   * @param teamName    The name of the team the user belongs to.
   * @param projectName The name of the project the user is associated with.
   * @param startDate   The start date of the report's date range (inclusive).
   * @param endDate     The end date of the report's date range (inclusive).
   *
   * @return A {@link ResponseEntity} containing the generated PDF report as a byte array,
   *         with the appropriate headers for downloading the file.
   *
   */
  @GetMapping("/user")
  @PreAuthorize("@expressionService.canAccessReport(#username, #teamName)")
  @Operation(
      summary = "Generate a PDF report for the user with additional filters",
      description = "Generates a PDF report with user tasks and performance",
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateUserReport(
      @Parameter(description = "The username of the user for whom the report is to be generated", example = "katy.zboncak@gmail.com")
      @RequestParam String username,

      @Parameter(description = "The name of the team the user belongs to", example = "Tennessee cats86c5")
      @RequestParam String teamName,

      @Parameter(description = "The name of the project the user is associated with", example = "Hoppe, Jacobson and Cole116c")
      @RequestParam String projectName,

      @Parameter(description = "The start date of the report's date range (inclusive)", example = "2025-01-01")
      @RequestParam String startDate,

      @Parameter(description = "The end date of the report's date range (inclusive)", example = "2025-12-31")
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildUserReport(username, teamName, projectName, startDate, endDate);
    String fileName = "user_report_" + username + "_" + teamName + "_" + projectName + "_" + startDate + "_to_" + endDate + ".pdf";

    return ResponseEntity.ok()
      .header("Content-Type", "application/pdf")
      .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
      .body(pdfData);
  }

  /**
   * Generates a PDF report for the top performers of a team.
   *
   * @param teamName  The name of the team.
   * @param startDate The start date of the report's date range.
   * @param endDate   The end date of the report's date range.
   * @return A {@link ResponseEntity} containing the generated PDF report.
   */
  @GetMapping("/top-performers")
  @PreAuthorize("@expressionService.canAccessReport(#teamName)")
  @Operation(
      summary = "Generate a PDF report for top performers",
      description = "Generates a PDF report displaying the top performers in the team for a given date range",
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateTopPerformersInTeamReport(
      @Parameter(description = "The name of the team", example = "Hawaii chickensd2f9")
      @RequestParam String teamName,

      @Parameter(description = "The start date of the report's date range", example = "2025-01-01")
      @RequestParam String startDate,

      @Parameter(description = "The end date of the report's date range", example = "2025-12-31")
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildTopPerformersInTeamReport(teamName, startDate, endDate);
    String fileName = "top_performers_report_" + teamName + "_" + startDate + "_to_" + endDate + ".pdf";

    return ResponseEntity.ok()
      .header("Content-Type", "application/pdf")
      .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
      .body(pdfData);
  }
}
