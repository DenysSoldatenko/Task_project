package com.example.taskmanagerproject.controllers;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TeamService;
import java.util.Collections;
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
 * Integration tests for {@link TeamController}, providing full line and branch coverage for all team-related endpoints.
 *
 * <p>This test suite verifies the functionality of the Team Management API, including team creation, retrieval, updating,
 * deletion, and user/team-role associations. All service-layer dependencies are mocked to ensure focused controller behavior
 * verification in isolation from business logic or persistence concerns.
 *
 * <p>Functional areas tested:
 * <ul>
 *   <li>Creating teams with valid and invalid input</li>
 *   <li>Fetching team details by name</li>
 *   <li>Retrieving users and their roles for a specific team</li>
 *   <li>Fetching projects associated with a team</li>
 *   <li>Updating existing team details with authorization checks</li>
 *   <li>Deleting teams and handling failures</li>
 *   <li>Adding users to teams and assigning them roles</li>
 * </ul>
 *
 * <p><strong>Limitations and Known Gaps:</strong></p>
 * <ul>
 *   <li>Authentication and authorization failure paths (e.g. missing or invalid JWT, forbidden access) are not explicitly tested</li>
 *   <li>Edge-case DTO validation scenarios (e.g. null or deeply malformed fields) are minimally tested</li>
 *   <li>Error responses such as 404 Not Found for missing teams or users are not asserted</li>
 *   <li>No coverage of domain-specific constraints or unique constraint violations</li>
 *   <li>Audit logging, security filters, or side effects are not verified</li>
 * </ul>
 *
 * <p>To enhance robustness and resilience, this test class can be complemented with:
 * <ul>
 *   <li>Security-focused tests for forbidden/unauthorized access attempts</li>
 *   <li>Additional edge-case and validation tests for all DTO inputs</li>
 *   <li>Contract or integration tests validating behavior across service boundaries</li>
 *   <li>Service-level tests for business rule enforcement and exception handling</li>
 * </ul>
 *
 * <p>This suite ensures controller-level correctness under normal and failure conditions,
 * and provides a foundation for broader test coverage at service and integration levels.</p>
 */
@WebMvcTest(controllers = TeamController.class)
class TeamControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private TeamService teamService;

  @MockBean
  private ProjectService projectService;

  private Jwt validJwt;
  private String teamName;
  private TeamDto teamDto;
  private ProjectTeamDto projectTeamDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();

    UserDto creator = new UserDto(1L, "creator", "creator@gmail.com", "alice-johnson-1234", "", null);
    teamDto = new TeamDto(1L, "Test Team", "Description", creator);
    ProjectDto projectDto = new ProjectDto(1L, "Test Project", "Description", creator);
    TeamDto teamDto = new TeamDto(1L, "Test Team", "Description", creator);
    projectTeamDto = new ProjectTeamDto(teamDto, projectDto);
    teamName = "Test Team";
    validJwt = Jwt.withTokenValue("valid-token")
      .header("alg", "HS256")
      .issuer("https://issuer.example.com")
      .subject("creator@gmail.com")
      .issuedAt(now())
      .expiresAt(now().plusSeconds(3600))
      .claim("email", "creator@gmail.com")
      .build();
  }

  @Nested
  @DisplayName("Create Team Tests")
  class CreateTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn201AndCreatedTeam() throws Exception {
      UserDto creatorDto = new UserDto(1L, "Alice Johnson", "alice12345@gmail.com", "alice-johnson-1234", "", null);
      TeamDto teamDto = new TeamDto(1L, "Test Team", "Description", creatorDto);

      when(teamService.createTeam(any(TeamDto.class))).thenReturn(teamDto);

      mockMvc.perform(post("/api/v2/teams")
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "name": "Test Team",
                "description": "Description",
                "creator": {
                  "fullName": "Alice Johnson",
                  "username": "alice12345@gmail.com",
                  "slug": "alice-johnson-1234"
                }
              }
            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Test Team")))
            .andExpect(jsonPath("$.description", is("Description")));

      verify(teamService).createTeam(any(TeamDto.class));
      verifyNoMoreInteractions(teamService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn400ForInvalidTeamDto() throws Exception {
      mockMvc.perform(post("/api/v2/teams")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "name": "",
              "description": "Description",
              "creator": { "id": 1 }
            }
          """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.createTeam(any(TeamDto.class))).thenThrow(new RuntimeException("Creation failed"));

      mockMvc.perform(post("/api/v2/teams")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "name": "Test Team",
              "description": "Description",
              "creator": { "id": 1 }
            }
          """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Creation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).createTeam(any(TeamDto.class));
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Get Team By Name Tests")
  class GetTeamByNameTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndTeam() throws Exception {
      when(teamService.getTeamByName(teamName)).thenReturn(teamDto);

      mockMvc.perform(get("/api/v2/teams/{teamName}", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Test Team")))
          .andExpect(jsonPath("$.description", is("Description")));

      verify(teamService).getTeamByName(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.getTeamByName(teamName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/teams/{teamName}", teamName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).getTeamByName(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Get Users With Roles For Team Tests")
  class GetUsersWithRolesForTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndUsersList() throws Exception {
      UserDto userDto = new UserDto(1L, "Alice Johnson", "alice12345@gmail.com", "alice-johnson-70be300f", "", List.of("004fdb21-bb00-4556-87c8-9a4e2198f6c7.jpg"));
      TeamDto teamDto = new TeamDto(1L, "Team Alpha", "This is a description of Team A", userDto);
      RoleDto roleDto = new RoleDto(1L, "ADMIN", "Administrator with full access");
      TeamUserDto teamUserDto = new TeamUserDto(userDto, teamDto, roleDto);

      when(teamService.getUsersWithRolesForTeam(teamName)).thenReturn(List.of(teamUserDto));

      mockMvc.perform(get("/api/v2/teams/{teamName}/users-roles", teamName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].user.id", is(1)))
          .andExpect(jsonPath("$[0].user.fullName", is("Alice Johnson")))
          .andExpect(jsonPath("$[0].user.username", is("alice12345@gmail.com")))
          .andExpect(jsonPath("$[0].user.slug", is("alice-johnson-70be300f")))
          .andExpect(jsonPath("$[0].user.image[0]", is("004fdb21-bb00-4556-87c8-9a4e2198f6c7.jpg")))
          .andExpect(jsonPath("$[0].role.id", is(1)))
          .andExpect(jsonPath("$[0].role.name", is("ADMIN")))
          .andExpect(jsonPath("$[0].role.description", is("Administrator with full access")));

      verify(teamService).getUsersWithRolesForTeam(teamName);
      verifyNoMoreInteractions(teamService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyList() throws Exception {
      when(teamService.getUsersWithRolesForTeam(teamName)).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/api/v2/teams/{teamName}/users-roles", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(teamService).getUsersWithRolesForTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.getUsersWithRolesForTeam(teamName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/teams/{teamName}/users-roles", teamName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).getUsersWithRolesForTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Get Projects For Team Tests")
  class GetProjectsForTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndProjectsList() throws Exception {
      when(projectService.getProjectsForTeam(teamName)).thenReturn(Collections.singletonList(projectTeamDto));

      mockMvc.perform(get("/api/v2/teams/{teamName}/projects", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].project.name", is("Test Project")))
          .andExpect(jsonPath("$[0].team.name", is("Test Team")));

      verify(projectService).getProjectsForTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyList() throws Exception {
      when(projectService.getProjectsForTeam(teamName)).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/api/v2/teams/{teamName}/projects", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(projectService).getProjectsForTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.getProjectsForTeam(teamName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/teams/{teamName}/projects", teamName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).getProjectsForTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Update Team Tests")
  class UpdateTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn200AndUpdatedTeam() throws Exception {
      UserDto userDto = new UserDto(1L, "Alice Johnson", "alice12345@gmail.com", "alice-johnson-1234", "", null);
      TeamDto teamDto = new TeamDto(1L, "Test Team 2", "Updated description", userDto);

      when(teamService.updateTeam(eq(teamName), any(TeamDto.class))).thenReturn(teamDto);

      mockMvc.perform(put("/api/v2/teams/{teamName}", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "name": "Test Team 2",
              "description": "Updated description",
              "creator": {
                "fullName": "Alice Johnson",
                "username": "alice12345@gmail.com",
                "slug": "alice-johnson-1234"
              }
            }
          """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("Test Team 2")))
          .andExpect(jsonPath("$.description", is("Updated description")));

      verify(teamService).updateTeam(eq(teamName), any(TeamDto.class));
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn400ForInvalidTeamDto() throws Exception {
      mockMvc.perform(put("/api/v2/teams/{teamName}", teamName)
            .with(jwt().jwt(validJwt).authorities(
              new SimpleGrantedAuthority("ROLE_USER"),
              new SimpleGrantedAuthority("TEAM_ACCESS_Test Team")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "name": "",
                "description": "Updated Description",
                "creator": { "id": 1 }
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.updateTeam(eq(teamName), any(TeamDto.class))).thenThrow(new RuntimeException("Update failed"));

      mockMvc.perform(put("/api/v2/teams/{teamName}", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "name": "Test Team",
              "description": "Updated Description",
              "creator": { "id": 1 }
            }
          """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Update failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).updateTeam(eq(teamName), any(TeamDto.class));
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Delete Team Tests")
  class DeleteTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn204WhenTeamDeleted() throws Exception {
      doNothing().when(teamService).deleteTeam(teamName);

      mockMvc.perform(delete("/api/v2/teams/{teamName}", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team"))
          )
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(teamService).deleteTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(teamService).deleteTeam(teamName);

      mockMvc.perform(delete("/api/v2/teams/{teamName}", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team"))
          )
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).deleteTeam(teamName);
      verifyNoMoreInteractions(teamService, projectService);
    }
  }

  @Nested
  @DisplayName("Add Users To Team Tests")
  class AddUsersToTeamTests {

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn201AndUsersList() throws Exception {
      UserDto userDto = new UserDto(1L, "Alice Johnson", "alice12345@gmail.com", "alice-johnson-1234", "", null);
      TeamDto teamDto = new TeamDto(1L, "Test Team", "This is a description of Team A", userDto);
      RoleDto roleDto = new RoleDto(14L, "USER", "Default user with the lowest access level");
      TeamUserDto teamUserDto = new TeamUserDto(userDto, teamDto, roleDto);

      when(teamService.addUsersToTeam(eq(teamName), anyList())).thenReturn(List.of(teamUserDto));

      mockMvc.perform(post("/api/v2/teams/{teamName}/users", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team")))
          .contentType(APPLICATION_JSON)
          .content("""
            [
              {
                "user": {
                  "fullName": "Alice Johnson",
                  "username": "alice12345@gmail.com",
                  "slug": "alice-johnson-1234"
                },
                "team": {
                  "name": "Test Team",
                  "description": "This is a description of Team A",
                  "creator": {
                    "fullName": "Alice Johnson",
                    "username": "alice12345@gmail.com",
                    "slug": "alice-johnson-1234"
                  }
                },
                "role": {
                  "id": "14",
                  "name": "USER",
                  "description": "Default user with the lowest access level"
                }
              }
            ]
          """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$[0].user.fullName", is("Alice Johnson")))
          .andExpect(jsonPath("$[0].user.username", is("alice12345@gmail.com")))
          .andExpect(jsonPath("$[0].user.slug", is("alice-johnson-1234")))
          .andExpect(jsonPath("$[0].role.id", is(14)))
          .andExpect(jsonPath("$[0].role.name", is("USER")))
          .andExpect(jsonPath("$[0].role.description", is("Default user with the lowest access level")));

      verify(teamService).addUsersToTeam(eq(teamName), anyList());
      verifyNoMoreInteractions(teamService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn201AndEmptyList() throws Exception {
      when(teamService.addUsersToTeam(eq(teamName), any())).thenReturn(Collections.emptyList());

      mockMvc.perform(post("/api/v2/teams/{teamName}/users", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team"))
          )
          .contentType(APPLICATION_JSON)
          .content("[]"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$").isEmpty());

      verify(teamService).addUsersToTeam(eq(teamName), any());
      verifyNoMoreInteractions(teamService, projectService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com", authorities = {"ROLE_USER", "TEAM_ACCESS_Test Team"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.addUsersToTeam(eq(teamName), any())).thenThrow(new RuntimeException("Addition failed"));

      mockMvc.perform(post("/api/v2/teams/{teamName}/users", teamName)
          .with(jwt().jwt(validJwt).authorities(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("TEAM_ACCESS_Test Team")))
          .contentType(APPLICATION_JSON)
          .content("""
            [
              {
                "user": {
                  "id": 1,
                  "username": "creator@gmail.com"
                },
                "role": {
                  "id": 5,
                  "name": "LEADER",
                  "description": "Team leader, overseeing team members"
                }
              }
            ]
          """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Addition failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).addUsersToTeam(eq(teamName), any());
      verifyNoMoreInteractions(teamService);
    }
  }
}