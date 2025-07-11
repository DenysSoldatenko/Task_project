package com.example.taskmanagerproject.controllers;

import com.example.taskmanagerproject.exceptions.errorhandling.ErrorDetails;
import com.example.taskmanagerproject.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
   * <p><strong>Note:</strong> This report is not generated for:</p>
   * <ul>
   *   <li><strong>Admin</strong> – excluded in all cases.</li>
   *   <li><strong>Product Owner</strong> – excluded only if they are the sole Product Owner in the team</li>
   * </ul>
   *
   * <p>If you attempt to generate a report for such users, the system will return a 500 Internal Server Error.</p>
   *
   * @param username    The email address of the user for whom the report is generated.
   * @param teamName    The name of the team the user belongs to.
   * @param projectName The name of the project the user is associated with.
   * @param startDate   The start date of the report's date range (inclusive).
   * @param endDate     The end date of the report's date range (inclusive).
   *
   * @return A {@link ResponseEntity} containing the generated PDF report.
   */
  @GetMapping("/user")
  @PreAuthorize("@expressionService.canAccessUserReport(#username, #teamName)")
  @Operation(
      summary = "Generate a PDF report for the user with additional filters",
      description = "Generates a PDF report with user tasks and performance",
      parameters = {
        @Parameter(name = "username", description = "The username of the user for whom the report is to be generated",
          required = true, in = ParameterIn.QUERY, example = "alice12345@gmail.com"),
        @Parameter(name = "teamName", description = "The name of the team the user belongs to",
          required = true, in = ParameterIn.QUERY, example = "Team Alpha"),
        @Parameter(name = "projectName", description = "The name of the project the user is associated with",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "startDate", description = "The start date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-01-01"),
        @Parameter(name = "endDate", description = "The end date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-12-01")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateUserReport(
      @RequestParam String username,
      @RequestParam String teamName,
      @RequestParam String projectName,
      @RequestParam String startDate,
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildUserReport(username, teamName, projectName, startDate, endDate);
    return buildPdfResponse(pdfData, formatFileName("user_report", username, teamName, projectName, startDate, endDate));
  }

  /**
   * Generates a PDF report for a team's overall performance and progress within a specified date range.
   *
   * @param teamName  The name of the team for which the report is generated.
   * @param projectName The name of the project associated with the team.
   * @param startDate The start date (inclusive) of the date range for which the report is generated.
   * @param endDate   The end date (inclusive) of the date range for which the report is generated.
   * @return A {@link ResponseEntity} containing the generated PDF report.
   */
  @GetMapping("/team-report")
  @PreAuthorize("@expressionService.canAccessTeamReport(#teamName)")
  @Operation(
      summary = "Generate a PDF report for team performance and progress",
      description = "Generates a PDF report displaying the overall performance and progress of the team for a given date range",
      parameters = {
        @Parameter(name = "teamName", description = "The name of the team",
          required = true, in = ParameterIn.QUERY, example = "Team Alpha"),
        @Parameter(name = "projectName", description = "The name of the project the team is associated with",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "startDate", description = "The start date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-01-01"),
        @Parameter(name = "endDate", description = "The end date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-12-31")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateTeamReport(
      @RequestParam String teamName,
      @RequestParam String projectName,
      @RequestParam String startDate,
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildTeamReport(teamName, projectName, startDate, endDate);
    return buildPdfResponse(pdfData, formatFileName("team_report", teamName, projectName, startDate, endDate));
  }

  /**
   * Generates a PDF report for a project's overall performance and progress within a specified date range.
   *
   * @param projectName The name of the project for which the report is generated.
   * @param startDate   The start date (inclusive) of the date range for which the report is generated.
   * @param endDate     The end date (inclusive) of the date range for which the report is generated.
   * @return A {@link ResponseEntity} containing the generated PDF report.
   */
  @GetMapping("/project-report")
  @PreAuthorize("@expressionService.canAccessProjectReport(#projectName)")
  @Operation(
      summary = "Generate a PDF report for project performance and progress",
      description = "Generates a PDF report displaying the overall performance and progress of the project for a given date range",
      parameters = {
        @Parameter(name = "projectName", description = "The name of the project",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "startDate", description = "The start date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-01-01"),
        @Parameter(name = "endDate", description = "The end date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-12-31")
      },
      responses = {
          @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
            content = @Content(mediaType = "application/pdf")),
          @ApiResponse(responseCode = "400", description = "Invalid input parameters",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized access",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "404", description = "Resource not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
          @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateProjectReport(
      @RequestParam String projectName,
      @RequestParam String startDate,
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildProjectReport(projectName, startDate, endDate);
    return buildPdfResponse(pdfData, formatFileName("project_report", projectName, startDate, endDate));
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
  @PreAuthorize("@expressionService.canAccessTeamReport(#teamName)")
  @Operation(
      summary = "Generate a PDF report for top performers",
      description = "Generates a PDF report displaying the top performers in the team for a given date range",
      parameters = {
        @Parameter(name = "teamName", description = "The name of the team the user belongs to",
          required = true, in = ParameterIn.QUERY, example = "Team Alpha"),
        @Parameter(name = "projectName", description = "The name of the project the user is associated with",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "startDate", description = "The start date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-01-01"),
        @Parameter(name = "endDate", description = "The end date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-12-01")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateTopPerformersInTeamReport(
      @RequestParam String teamName,
      @RequestParam String projectName,
      @RequestParam String startDate,
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildTopPerformersInTeamReport(teamName, projectName, startDate, endDate);
    return buildPdfResponse(pdfData, formatFileName("top_performers_report", teamName, projectName, startDate, endDate));
  }

  /**
   * Generates a PDF report showing the task progress within a specified team and project for a given date range.
   *
   * @param username    The email address of the user requesting the report (used for access control).
   * @param teamName    The name of the team for which the task progress report is generated.
   * @param projectName The name of the project associated with the task progress.
   * @param startDate   The start date (inclusive) of the date range for which the report is generated.
   * @param endDate     The end date (inclusive) of the date range for which the report is generated.
   *
   * @return A {@link ResponseEntity} containing the generated PDF report.
   */
  @GetMapping("/task-progress")
  @PreAuthorize("@expressionService.canAccessUserReport(#username, #teamName)")
  @Operation(
      summary = "Generate a PDF report for task progress",
      description = "Generates a PDF report showing the task progress within a team for a given date range",
      parameters = {
        @Parameter(name = "username", description = "The username of the user for whom the report is to be generated",
          required = true, in = ParameterIn.QUERY, example = "alice12345@gmail.com"),
        @Parameter(name = "teamName", description = "The name of the team the user belongs to",
          required = true, in = ParameterIn.QUERY, example = "Team Alpha"),
        @Parameter(name = "projectName", description = "The name of the project the user is associated with",
          required = true, in = ParameterIn.QUERY, example = "Project Alpha"),
        @Parameter(name = "startDate", description = "The start date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-01-01"),
        @Parameter(name = "endDate", description = "The end date of the report's date range (inclusive)",
          required = true, in = ParameterIn.QUERY, example = "2025-12-01")
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "PDF report generated successfully",
          content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
      }
  )
  public ResponseEntity<byte[]> generateTaskProgressReport(
      @RequestParam String username,
      @RequestParam String teamName,
      @RequestParam String projectName,
      @RequestParam String startDate,
      @RequestParam String endDate
  ) {
    byte[] pdfData = reportService.buildTaskProgressReport(username, teamName, projectName, startDate, endDate);
    return buildPdfResponse(pdfData, formatFileName("task_progress_report", username, teamName, projectName, startDate, endDate));
  }


  private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfData, String fileName) {
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
      .body(pdfData);
  }

  private String formatFileName(String prefix, String... parts) {
    return prefix + "_" + String.join("_", parts).replaceAll("\\s+", "_") + ".pdf";
  }
}
