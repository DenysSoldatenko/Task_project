package com.example.taskmanagerproject.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.services.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link ReportController}, ensuring correctness of report generation endpoints.
 *
 * <p>These tests validate PDF-based reporting functionality for various domain contexts, including:
 * <ul>
 *   <li>User-specific reports</li>
 *   <li>Team-wide reports</li>
 *   <li>Project-wide reports</li>
 *   <li>Top performers within a team</li>
 *   <li>User task progress tracking</li>
 * </ul>
 *
 * <p>Reports are generated in PDF format and delivered via HTTP responses with appropriate headers. The tests
 * assert correct status codes, content types, file naming conventions, and response body content.
 * Services are mocked to isolate the controller behavior.
 *
 * <p><strong>Known Gaps and Limitations:</strong></p>
 * The following edge cases and error-handling paths are currently not covered:
 * <ul>
 *   <li>Authorization failures (e.g. missing roles or insufficient privileges)</li>
 *   <li>Malformed or expired JWT tokens</li>
 *   <li>Validation failures for request parameters (e.g. missing or invalid dates)</li>
 *   <li>Edge cases in report content generation (e.g. empty datasets, corrupted files)</li>
 *   <li>Specific business rule violations (e.g. disallowed roles like sole Product Owner)</li>
 * </ul>
 *
 * <p>Due to controller-service abstraction, some business rules are only partially observable here.
 * Consider complementing with service-layer unit tests to assert:
 * <ul>
 *   <li>Proper enforcement of domain rules</li>
 *   <li>Fallback behavior and exception handling logic</li>
 *   <li>Data formatting and report content integrity</li>
 * </ul>
 *
 * <p>This suite achieves full line coverage for the controller but does not claim to exhaustively
 * exercise all report generation permutations or edge conditions.</p>
 */
@WebMvcTest(controllers = ReportController.class)
class ReportControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private ReportService reportService;

  private String username;
  private String teamName;
  private String projectName;
  private String startDate;
  private String endDate;
  private byte[] pdfData;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();
    username = "alice12345@gmail.com";
    teamName = "Team Alpha";
    projectName = "Project Alpha";
    startDate = "2025-01-01";
    endDate = "2025-12-01";
    pdfData = new byte[]{1, 2, 3};
  }

  @Nested
  @DisplayName("Generate User Report Tests")
  class GenerateUserReportTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndPdf() throws Exception {
      when(reportService.buildUserReport(username, teamName, projectName, startDate, endDate)).thenReturn(pdfData);

      mockMvc.perform(get("/api/v2/reports/user")
          .param("username", username)
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isOk())
          .andExpect(header().string(CONTENT_TYPE, APPLICATION_PDF_VALUE))
          .andExpect(header().string(CONTENT_DISPOSITION,
            is("attachment; filename=\"user_report_alice12345@gmail.com_Team_Alpha_Project_Alpha_2025-01-01_2025-12-01.pdf\"")))
          .andExpect(content().bytes(pdfData));

      verify(reportService).buildUserReport(username, teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500ForAdminUser() throws Exception {
      when(reportService.buildUserReport(username, teamName, projectName, startDate, endDate)).thenThrow(new RuntimeException("Report not generated for Admin"));

      mockMvc.perform(get("/api/v2/reports/user")
          .param("username", username)
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Report not generated for Admin")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(reportService).buildUserReport(username, teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }
  }

  @Nested
  @DisplayName("Generate Team Report Tests")
  class GenerateTeamReportTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndPdf() throws Exception {
      when(reportService.buildTeamReport(teamName, projectName, startDate, endDate)).thenReturn(pdfData);

      mockMvc.perform(get("/api/v2/reports/team-report")
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isOk())
          .andExpect(header().string(CONTENT_TYPE, APPLICATION_PDF_VALUE))
          .andExpect(header().string(CONTENT_DISPOSITION,
            is("attachment; filename=\"team_report_Team_Alpha_Project_Alpha_2025-01-01_2025-12-01.pdf\"")))
          .andExpect(content().bytes(pdfData));

      verify(reportService).buildTeamReport(teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(reportService.buildTeamReport(teamName, projectName, startDate, endDate)).thenThrow(new RuntimeException("Report generation failed"));

      mockMvc.perform(get("/api/v2/reports/team-report")
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Report generation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(reportService).buildTeamReport(teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }
  }

  @Nested
  @DisplayName("Generate Project Report Tests")
  class GenerateProjectReportTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndPdf() throws Exception {
      when(reportService.buildProjectReport(projectName, startDate, endDate)).thenReturn(pdfData);

      mockMvc.perform(get("/api/v2/reports/project-report")
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isOk())
          .andExpect(header().string(CONTENT_TYPE, APPLICATION_PDF_VALUE))
          .andExpect(header().string(CONTENT_DISPOSITION,
            is("attachment; filename=\"project_report_Project_Alpha_2025-01-01_2025-12-01.pdf\"")))
          .andExpect(content().bytes(pdfData));

      verify(reportService).buildProjectReport(projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(reportService.buildProjectReport(projectName, startDate, endDate)).thenThrow(new RuntimeException("Report generation failed"));

      mockMvc.perform(get("/api/v2/reports/project-report")
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Report generation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(reportService).buildProjectReport(projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }
  }

  @Nested
  @DisplayName("Generate Top Performers Report Tests")
  class GenerateTopPerformersReportTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndPdf() throws Exception {
      when(reportService.buildTopPerformersInTeamReport(teamName, projectName, startDate, endDate)).thenReturn(pdfData);

      mockMvc.perform(get("/api/v2/reports/top-performers")
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isOk())
          .andExpect(header().string(CONTENT_TYPE, APPLICATION_PDF_VALUE))
          .andExpect(header().string(CONTENT_DISPOSITION,
            is("attachment; filename=\"top_performers_report_Team_Alpha_Project_Alpha_2025-01-01_2025-12-01.pdf\"")))
          .andExpect(content().bytes(pdfData));

      verify(reportService).buildTopPerformersInTeamReport(teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(reportService.buildTopPerformersInTeamReport(teamName, projectName, startDate, endDate)).thenThrow(new RuntimeException("Report generation failed"));

      mockMvc.perform(get("/api/v2/reports/top-performers")
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Report generation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(reportService).buildTopPerformersInTeamReport(teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }
  }

  @Nested
  @DisplayName("Generate Task Progress Report Tests")
  class GenerateTaskProgressReportTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndPdf() throws Exception {
      when(reportService.buildTaskProgressReport(username, teamName, projectName, startDate, endDate)).thenReturn(pdfData);

      mockMvc.perform(get("/api/v2/reports/task-progress")
          .param("username", username)
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isOk())
          .andExpect(header().string(CONTENT_TYPE, APPLICATION_PDF_VALUE))
          .andExpect(header().string(CONTENT_DISPOSITION,
            is("attachment; filename=\"task_progress_report_alice12345@gmail.com_Team_Alpha_Project_Alpha_2025-01-01_2025-12-01.pdf\"")))
          .andExpect(content().bytes(pdfData));

      verify(reportService).buildTaskProgressReport(username, teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500ForSoleProductOwner() throws Exception {
      when(reportService.buildTaskProgressReport(username, teamName, projectName, startDate, endDate)).thenThrow(new RuntimeException("Report not allowed for sole Product Owner"));

      mockMvc.perform(get("/api/v2/reports/task-progress")
          .param("username", username)
          .param("teamName", teamName)
          .param("projectName", projectName)
          .param("startDate", startDate)
          .param("endDate", endDate))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Report not allowed for sole Product Owner")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(reportService).buildTaskProgressReport(username, teamName, projectName, startDate, endDate);
      verifyNoMoreInteractions(reportService);
    }
  }
}