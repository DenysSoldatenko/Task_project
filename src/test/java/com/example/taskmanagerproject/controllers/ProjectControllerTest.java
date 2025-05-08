package com.example.taskmanagerproject.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.services.ProjectService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link ProjectController}, providing full line coverage.
 *
 * <p>These tests validate the main controller endpoints for managing projects and their
 * associations with teams. Scenarios covered include project creation, retrieval,
 * update, deletion, and team assignment. The controller is tested in isolation using
 * Spring MVC's {@code MockMvc}, with dependent services mocked.
 *
 * <p><strong>Known Limitations and Deferred Scenarios:</strong></p>
 * While coverage is complete for standard operations and basic error handling, the following
 * edge cases and exceptional conditions are not yet covered:
 *
 * <ul>
 *   <li>Authorization failures (e.g. invalid roles, JWT tokens with insufficient privileges)</li>
 *   <li>JWT tokens with missing, malformed, or expired claims</li>
 *   <li>Validation of nested DTOs (e.g. malformed or null {@code TeamDto} or {@code ProjectDto})</li>
 *   <li>Business logic exceptions beyond simple {@code RuntimeException} (e.g. {@code EntityNotFoundException})</li>
 * </ul>
 *
 * <p>To improve test granularity, consider complementing these integration tests with
 * unit tests for the service and validation layers, where mocking and exception simulation
 * are more manageable.</p>
 */
@WebMvcTest(controllers = ProjectController.class)
class ProjectControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private ProjectService projectService;

  private Jwt validJwt;
  private String projectName;
  private ProjectDto projectDto;
  private ProjectTeamDto projectTeamDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();
    projectName = "Project Alpha";
    UserDto userDto = new UserDto(1L, "Test User", "user@gmail.com", "user-slug", "", List.of(""));
    projectDto = new ProjectDto(1L, "Project Alpha", "Description", userDto);
    TeamDto teamDto = new TeamDto(1L, "Test Team", "Description", userDto);
    projectTeamDto = new ProjectTeamDto(teamDto, projectDto);
    validJwt = Jwt.withTokenValue("valid-token")
      .header("alg", "HS256")
      .issuer("https://issuer.example.com")
      .subject("user@example.com")
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plusSeconds(3600))
      .claim("email", "user@example.com")
      .build();
  }

  @Nested
  @DisplayName("Create Project Tests")
  class CreateProjectTests {

    @Test
    void shouldReturn201AndCreatedProject() throws Exception {
      when(projectService.createProject(any(ProjectDto.class))).thenReturn(projectDto);

      mockMvc.perform(post("/api/v2/projects")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"Project Alpha\",\"description\":\"Description\"}"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Project Alpha")))
          .andExpect(jsonPath("$.description", is("Description")));

      verify(projectService).createProject(any(ProjectDto.class));
      verifyNoMoreInteractions(projectService);
    }

    @Test
    void shouldReturn400ForInvalidProjectDto() throws Exception {
      mockMvc.perform(post("/api/v2/projects")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"\",\"description\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.createProject(any(ProjectDto.class))).thenThrow(new RuntimeException("Creation failed"));

      mockMvc.perform(post("/api/v2/projects")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"Project Alpha\",\"description\":\"Description\"}"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Creation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).createProject(any(ProjectDto.class));
      verifyNoMoreInteractions(projectService);
    }
  }

  @Nested
  @DisplayName("Get Project By Name Tests")
  class GetProjectByNameTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndProject() throws Exception {
      when(projectService.getProjectByName(projectName)).thenReturn(projectDto);

      mockMvc.perform(get("/api/v2/projects/{projectName}", projectName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Project Alpha")))
          .andExpect(jsonPath("$.description", is("Description")));

      verify(projectService).getProjectByName(projectName);
      verifyNoMoreInteractions(projectService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.getProjectByName(projectName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/projects/{projectName}", projectName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).getProjectByName(projectName);
      verifyNoMoreInteractions(projectService);
    }
  }

  @Nested
  @DisplayName("Get Teams For Project Tests")
  class GetTeamsForProjectTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndTeams() throws Exception {
      when(projectService.getTeamsForProject(projectName)).thenReturn(List.of(projectTeamDto));

      mockMvc.perform(get("/api/v2/projects/{projectName}/teams", projectName)).andExpect(status().isOk());

      verify(projectService).getTeamsForProject(projectName);
      verifyNoMoreInteractions(projectService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.getTeamsForProject(projectName)).thenThrow(new RuntimeException("Team retrieval failed"));

      mockMvc.perform(get("/api/v2/projects/{projectName}/teams", projectName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Team retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).getTeamsForProject(projectName);
      verifyNoMoreInteractions(projectService);
    }
  }

  @Nested
  @DisplayName("Update Project Tests")
  class UpdateProjectTests {

    @Test
    void shouldReturn200AndUpdatedProject() throws Exception {
      when(projectService.updateProject(eq(projectName), any(ProjectDto.class))).thenReturn(projectDto);

      mockMvc.perform(put("/api/v2/projects/{projectName}", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"Project Alpha\",\"description\":\"Updated Description\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Project Alpha")))
          .andExpect(jsonPath("$.description", is("Description")));

      verify(projectService).updateProject(eq(projectName), any(ProjectDto.class));
    }

    @Test
    void shouldReturn400ForInvalidProjectDto() throws Exception {

      mockMvc.perform(put("/api/v2/projects/{projectName}", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"\",\"description\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(projectService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.updateProject(eq(projectName), any(ProjectDto.class))).thenThrow(new RuntimeException("Update failed"));

      mockMvc.perform(put("/api/v2/projects/{projectName}", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("{\"name\":\"Project Alpha\",\"description\":\"Updated Description\"}"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Update failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).updateProject(eq(projectName), any(ProjectDto.class));
      verifyNoMoreInteractions(projectService);
    }
  }

  @Nested
  @DisplayName("Delete Project Tests")
  class DeleteProjectTests {

    @Test
    void shouldReturn204WhenProjectDeleted() throws Exception {
      doNothing().when(projectService).deleteProject(projectName);

      mockMvc.perform(delete("/api/v2/projects/{projectName}", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(projectService).deleteProject(projectName);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(projectService).deleteProject(projectName);

      mockMvc.perform(delete("/api/v2/projects/{projectName}", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).deleteProject(projectName);
      verifyNoMoreInteractions(projectService);
    }
  }

  @Nested
  @DisplayName("Add Team To Project Tests")
  class AddTeamToProjectTests {

    @Test
    void shouldReturn201AndUpdatedProject() throws Exception {
      when(projectService.addTeamToProject(eq(projectName), any())).thenReturn(projectDto);

      mockMvc.perform(post("/api/v2/projects/{projectName}/teams", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("[{\"teamId\":1,\"role\":\"Developer\"}]"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Project Alpha")))
          .andExpect(jsonPath("$.description", is("Description")));

      verify(projectService).addTeamToProject(eq(projectName), any());
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.addTeamToProject(eq(projectName), any())).thenThrow(new RuntimeException("Team addition failed"));

      mockMvc.perform(post("/api/v2/projects/{projectName}/teams", projectName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("[{\"teamId\":1,\"role\":\"Developer\"}]"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Team addition failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).addTeamToProject(eq(projectName), any());
    }
  }
}