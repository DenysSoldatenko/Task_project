package com.example.taskmanagerproject.controllers;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.UserService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link UserController}, achieving complete line coverage for all user-related endpoints.
 *
 * <p>These tests validate the behavior of the user management API, including CRUD operations, photo management,
 * and retrieval of associated entities (projects, teams, tasks). All service dependencies are mocked to ensure
 * isolated controller behavior.
 *
 * <p>Functional areas tested:
 * <ul>
 *   <li>Fetching user details by slug</li>
 *   <li>Updating and deleting users with JWT-based authentication</li>
 *   <li>Uploading, updating, and deleting user profile images</li>
 *   <li>Retrieving associated projects, teams, and assigned/assigned-by tasks</li>
 *   <li>Pagination and filtering of task queries</li>
 *   <li>Graceful handling of service-layer exceptions</li>
 * </ul>
 *
 * <p>Test coverage includes:
 * <ul>
 *   <li>HTTP status validation (200, 204, 400, 500)</li>
 *   <li>Response body assertions using {@code jsonPath}</li>
 *   <li>Mocked interaction verification with {@code UserService}, {@code ProjectService}, {@code TeamService}, and {@code TaskService}</li>
 *   <li>Simulated authentication using {@code @WithMockUser} and {@code jwt()} post-processors</li>
 * </ul>
 *
 * <p><strong>Limitations and Known Gaps:</strong></p>
 * <ul>
 *   <li>Edge-case DTO validation (e.g., missing or malformed fields) is partially tested</li>
 *   <li>Authorization failure scenarios (e.g., forbidden access) are not explicitly covered</li>
 *   <li>No tests assert image file format constraints or size limitations</li>
 *   <li>404 or 403 responses for missing or unauthorized resource access are not tested</li>
 * </ul>
 *
 * <p>For comprehensive assurance, consider augmenting this suite with:
 * <ul>
 *   <li>Negative tests targeting role-based access control and authorization edge cases</li>
 *   <li>Additional input validation and malformed payload scenarios</li>
 *   <li>Security-focused tests around photo upload and resource access boundaries</li>
 *   <li>Contract or integration tests if downstream services (e.g., S3 for images) are involved</li>
 * </ul>
 *
 * <p>This test suite provides reliable coverage of the {@code UserController} logic and should be
 * complemented with service-layer, validation, and security-specific tests to ensure full stack integrity.</p>
 */
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private UserService userService;

  @MockBean
  private ProjectService projectService;

  @MockBean
  private TeamService teamService;

  @MockBean
  private TaskService taskService;

  private String slug;
  private Jwt validJwt;
  private UserDto userDto;
  private TeamDto teamDto;
  private TaskDto taskDto;
  private ProjectDto projectDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();

    userDto = new UserDto(1L, "Alice Johnson", "alice.johnson@gmail.com", "alice-johnson-89123073", "password", List.of("ROLE_USER"));
    UserDto receiverDto = new UserDto(2L, "receiver", "receiver@gmail.com", "", "", null);
    projectDto = new ProjectDto(1L, "Project Alpha", "Project Description", userDto);
    teamDto = new TeamDto(1L, "Team Alpha", "Team Description", userDto);
    taskDto = new TaskDto(
      1L, projectDto, null, "Fix the bug in the login module", "Fix the bug in the login module",
      now(), now().plusDays(5), now().plusDays(3), APPROVED, CRITICAL, receiverDto, userDto, null
    );
    slug = "alice-johnson-89123073";
    validJwt = Jwt.withTokenValue("valid-token")
      .header("alg", "HS256")
      .issuer("https://issuer.example.com")
      .subject("alice.johnson@gmail.com")
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plusSeconds(3600))
      .claim("email", "alice.johnson@gmail.com")
      .build();
  }

  @Nested
  @DisplayName("Get User By Slug Tests")
  class GetUserBySlugTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndUser() throws Exception {
      when(userService.getUserBySlug(slug)).thenReturn(userDto);

      mockMvc.perform(get("/api/v2/users/{slug}", slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.username", is("alice.johnson@gmail.com")))
          .andExpect(jsonPath("$.fullName", is("Alice Johnson")));

      verify(userService).getUserBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(userService.getUserBySlug(slug)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/users/{slug}", slug))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(userService).getUserBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Update User Tests")
  class UpdateUserTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndUpdatedUser() throws Exception {
      UserDto userDto = new UserDto(1L, "Alice Cathrine Johnson", "alice12345@gmail.com", "alice-johnson-1234", "", List.of("image1.png"));
      when(userService.updateUser(any(UserDto.class), eq(slug))).thenReturn(userDto);

      mockMvc.perform(put("/api/v2/users/{slug}", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "fullName": "Alice Cathrine Johnson",
              "username": "alice12345@gmail.com",
              "slug": "alice-johnson-1234",
              "image": [
                "image1.png"
              ]
            }
          """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.username", is("alice12345@gmail.com")))
          .andExpect(jsonPath("$.fullName", is("Alice Cathrine Johnson")));

      verify(userService).updateUser(any(UserDto.class), eq(slug));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn400ForInvalidUserDto() throws Exception {
      mockMvc.perform(put("/api/v2/users/{slug}", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
            {
              "username": "",
              "email": "alice.johnson@gmail.com",
              "firstName": "Alice",
              "lastName": "Johnson",
              "roles": ["ROLE_USER"]
            }
          """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Delete User By Slug Tests")
  class DeleteUserBySlugTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn204WhenUserDeleted() throws Exception {
      doNothing().when(userService).deleteUserBySlug(slug);

      mockMvc.perform(delete("/api/v2/users/{slug}", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(userService).deleteUserBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(userService).deleteUserBySlug(slug);

      mockMvc.perform(delete("/api/v2/users/{slug}", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(userService).deleteUserBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Get Projects By User Slug Tests")
  class GetProjectsByUserSlugTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndProjectsList() throws Exception {
      when(projectService.getProjectsBySlug(slug)).thenReturn(singletonList(projectDto));

      mockMvc.perform(get("/api/v2/users/{slug}/projects", slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].id", is(1)))
          .andExpect(jsonPath("$[0].name", is("Project Alpha")))
          .andExpect(jsonPath("$[0].description", is("Project Description")));

      verify(projectService).getProjectsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyList() throws Exception {
      when(projectService.getProjectsBySlug(slug)).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/api/v2/users/{slug}/projects", slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(projectService).getProjectsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(projectService.getProjectsBySlug(slug)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/users/{slug}/projects", slug))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(projectService).getProjectsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Get Teams By User Slug Tests")
  class GetTeamsByUserSlugTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndTeamsList() throws Exception {
      when(teamService.getTeamsBySlug(slug)).thenReturn(singletonList(teamDto));

      mockMvc.perform(get("/api/v2/users/{slug}/teams", slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].id", is(1)))
          .andExpect(jsonPath("$[0].name", is("Team Alpha")))
          .andExpect(jsonPath("$[0].description", is("Team Description")));

      verify(teamService).getTeamsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyList() throws Exception {
      when(teamService.getTeamsBySlug(slug)).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/api/v2/users/{slug}/teams", slug))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(teamService).getTeamsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(teamService.getTeamsBySlug(slug)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/users/{slug}/teams", slug))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(teamService).getTeamsBySlug(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Get Tasks Assigned To User Tests")
  class GetTasksAssignedToUserTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndTasksPage() throws Exception {
      Page<TaskDto> taskPage = new PageImpl<>(singletonList(taskDto), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
      when(taskService.getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenReturn(taskPage);

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-to", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id", is(1)))
          .andExpect(jsonPath("$.content[0].title", is("Fix the bug in the login module")))
          .andExpect(jsonPath("$.content[0].taskStatus", is("APPROVED")))
          .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
          .andExpect(jsonPath("$.pageable.pageSize", is(10)))
          .andExpect(jsonPath("$.totalElements", is(1)));

      verify(taskService).getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyPage() throws Exception {
      Page<TaskDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.by("id").ascending()), 0);
      when(taskService.getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenReturn(emptyPage);

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-to", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isEmpty())
          .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
          .andExpect(jsonPath("$.pageable.pageSize", is(10)))
          .andExpect(jsonPath("$.totalElements", is(0)));

      verify(taskService).getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskService.getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-to", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).getAllTasksAssignedToUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Get Tasks Assigned By User Tests")
  class GetTasksAssignedByUserTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndTasksPage() throws Exception {
      Page<TaskDto> taskPage = new PageImpl<>(singletonList(taskDto), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
      when(taskService.getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenReturn(taskPage);

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-by", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id", is(1)))
          .andExpect(jsonPath("$.content[0].title", is("Fix the bug in the login module")))
          .andExpect(jsonPath("$.content[0].taskStatus", is("APPROVED")))
          .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
          .andExpect(jsonPath("$.pageable.pageSize", is(10)))
          .andExpect(jsonPath("$.totalElements", is(1)));

      verify(taskService).getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200AndEmptyPage() throws Exception {
      Page<TaskDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.by("id").ascending()), 0);
      when(taskService.getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenReturn(emptyPage);

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-by", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isEmpty())
          .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
          .andExpect(jsonPath("$.pageable.pageSize", is(10)))
          .andExpect(jsonPath("$.totalElements", is(0)));

      verify(taskService).getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskService.getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class))).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/users/{slug}/tasks/assigned-by", slug)
          .param("projectName", "Project Alpha")
          .param("teamName", "Team Alpha")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).getAllTasksAssignedByUser(eq(slug), eq("Project Alpha"), eq("Team Alpha"), any(PageRequest.class));
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }

  @Nested
  @DisplayName("Upload User Photo Tests")
  class UploadUserPhotoTests {

    @Test
    void shouldReturn201WhenUserPhotoUploaded() throws Exception {
      doNothing().when(userService).uploadUserPhoto(eq(slug), any(UserImageDto.class));
      MockMultipartFile mockFile = new MockMultipartFile("file", "image.png", IMAGE_PNG_VALUE, "dummy image content".getBytes());

      mockMvc.perform(multipart("/api/v2/users/{slug}/image", slug)
          .file(mockFile)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(content().string(""));

      verify(userService).uploadUserPhoto(eq(slug), any(UserImageDto.class));
      verifyNoMoreInteractions(userService);
    }
  }

  @Nested
  @DisplayName("Update User Photo Tests")
  class UpdateUserPhotoTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn200WhenPhotoUpdated() throws Exception {
      doNothing().when(userService).updateUserPhoto(eq(slug), any(UserImageDto.class));
      MockMultipartFile mockFile = new MockMultipartFile("file", "image.png", IMAGE_PNG_VALUE, "dummy image content".getBytes());

      mockMvc.perform(multipart("/api/v2/users/{slug}/image", slug)
          .file(mockFile)
          .with(request -> {
            request.setMethod("PUT");
            return request;
          })
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(content().string(""));

      verify(userService).updateUserPhoto(eq(slug), any(UserImageDto.class));
      verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldReturn400ForInvalidImageDto() throws Exception {
      mockMvc.perform(post("/api/v2/users/{slug}/image", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
              {
                "name": "",
                "file": "base64data"
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(userService);
    }
  }

  @Nested
  @DisplayName("Delete User Photo Tests")
  class DeleteUserPhotoTests {

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn204WhenPhotoDeleted() throws Exception {
      doNothing().when(userService).deleteUserPhoto(slug);

      mockMvc.perform(delete("/api/v2/users/{slug}/image", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(userService).deleteUserPhoto(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }

    @Test
    @WithMockUser(username = "alice.johnson@gmail.com", authorities = {"ROLE_USER"})
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(userService).deleteUserPhoto(slug);

      mockMvc.perform(delete("/api/v2/users/{slug}/image", slug)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(userService).deleteUserPhoto(slug);
      verifyNoMoreInteractions(userService, projectService, teamService, taskService);
    }
  }
}