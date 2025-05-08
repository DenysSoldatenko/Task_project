package com.example.taskmanagerproject.controllers;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
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
import com.example.taskmanagerproject.dtos.tasks.TaskImageDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.services.TaskService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link TaskController}, providing complete line coverage for all task-related endpoints.
 *
 * <p>These tests validate the behavior of the task management API, including creation, updating, deletion,
 * retrieval, image handling, and time-sensitive queries. All service-layer dependencies are mocked to ensure
 * isolated controller-level testing.
 *
 * <p>Functional areas tested:
 * <ul>
 *   <li>Creating tasks with valid and invalid payloads</li>
 *   <li>Fetching tasks by ID</li>
 *   <li>Updating existing tasks with updated details</li>
 *   <li>Deleting tasks and verifying appropriate status codes</li>
 *   <li>Uploading, updating, and deleting task-associated images</li>
 *   <li>Retrieving soon-to-expire tasks with query filtering</li>
 *   <li>Handling service-layer exceptions gracefully</li>
 * </ul>
 *
 * <p>Test coverage includes response status validation, response body assertions, and
 * verification of service interactions. Authentication is simulated using Spring Security's
 * {@code @WithMockUser} and JWT-based {@code jwt()} request post-processors.
 *
 * <p><strong>Limitations and Known Gaps:</strong></p>
 * <ul>
 *   <li>Edge-case validations (e.g., field constraints, date inconsistencies) are minimally tested</li>
 *   <li>Authorization failures (e.g., missing roles, insufficient privileges) are not explicitly verified</li>
 *   <li>Domain-specific constraints (e.g., project-team-task alignment) are not enforced in test data</li>
 *   <li>Concurrency scenarios and transactional rollbacks are not simulated</li>
 *   <li>Error response validation for 404 (not found) and 403 (forbidden) cases is not comprehensive</li>
 * </ul>
 *
 * <p>For more comprehensive coverage, consider extending this suite with:
 * <ul>
 *   <li>Unit tests targeting validation, transformation, and exception handling logic</li>
 *   <li>Security tests focused on access control and authority boundaries</li>
 *   <li>Contract or component-level tests that validate integration with downstream services or persistence layers</li>
 * </ul>
 *
 * <p>This suite provides controller-layer validation under mocked conditions and should be complemented
 * with broader testing efforts across validation, authorization, and integration layers for full system assurance.</p>
 */
@WebMvcTest(controllers = TaskController.class)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private TaskService taskService;

  private Long taskId;
  private Jwt validJwt;
  private TaskDto taskDto;
  private String username;
  private String teamName;
  private String imageName;
  private UserDto senderDto;
  private Duration duration;
  private String projectName;
  private UserDto receiverDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();

    senderDto = new UserDto(1L, "sender", "sender@gmail.com", "", "", null);
    receiverDto = new UserDto(2L, "receiver", "receiver@gmail.com", "", "", null);
    ProjectDto projectDto = new ProjectDto(1L, "Project Alpha", "This is a description of Project Alpha", senderDto);
    taskDto = new TaskDto(
      1L, projectDto, null, "Fix the bug in the login module", "Fix the bug in the login module",
      now(), now().plusDays(5), now().plusDays(3), APPROVED, CRITICAL, receiverDto, senderDto, null
    );
    taskId = 1L;
    imageName = "image.png";
    username = "creator@gmail.com";
    projectName = "Project Alpha";
    teamName = "Team Alpha";
    duration = Duration.parse("PT3H");
    validJwt = Jwt.withTokenValue("valid-token")
      .header("alg", "HS256")
      .issuer("https://issuer.example.com")
      .subject("creator@gmail.com")
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plusSeconds(3600))
      .claim("email", "creator@gmail.com")
      .build();
  }

  @Nested
  @DisplayName("Create Task Tests")
  class CreateTaskTests {

    @Test
    void shouldReturn201AndCreatedTask() throws Exception {
      when(taskService.createTaskForUser(any(TaskDto.class))).thenReturn(taskDto);

      mockMvc.perform(post("/api/v2/tasks")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
                {
                  "project": {
                    "name": "Project Alpha",
                    "description": "This is a description of Project Alpha",
                    "creator": {
                      "fullName": "Alice Johnson",
                      "username": "alice12345@gmail.com",
                      "slug": "alice-johnson-1234"
                    }
                  },
                  "team": {
                    "name": "Team Alpha",
                    "description": "This is a description of Team A",
                    "creator": {
                      "fullName": "Alice Johnson",
                      "username": "alice12345@gmail.com",
                      "slug": "alice-johnson-1234"
                    }
                  },
                  "title": "Fix the bug in the login module",
                  "description": "Fix the bug that causes login to fail for users with special characters in their password",
                  "expirationDate": "2025-06-26T13:24:27.234Z",
                  "taskStatus": "APPROVED",
                  "priority": "CRITICAL",
                  "assignedTo": {
                    "fullName": "Alice Johnson",
                    "username": "receiver@gmail.com",
                    "slug": "alice-johnson-1234"
                  },
                  "assignedBy": {
                    "fullName": "Duke Johnson",
                    "username": "sender@gmail.com",
                    "slug": "duke-johnson-1234"
                  }
                }
            """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.title", is("Fix the bug in the login module")))
          .andExpect(jsonPath("$.project.name", is("Project Alpha")))
          .andExpect(jsonPath("$.assignedTo.username", is("receiver@gmail.com")))
          .andExpect(jsonPath("$.taskStatus", is("APPROVED")));

      verify(taskService).createTaskForUser(any(TaskDto.class));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn400ForInvalidTaskDto() throws Exception {
      mockMvc.perform(post("/api/v2/tasks")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
              {
                "project": { "id": 1 },
                "title": "",
                "description": "Task Description",
                "createdAt": "2025-06-26T16:00:00",
                "dueDate": "2025-07-01T16:00:00",
                "updatedAt": "2025-06-29T16:00:00",
                "status": "APPROVED",
                "priority": "CRITICAL",
                "assignee": { "id": 2 },
                "creator": { "id": 1 }
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Get Task By ID Tests")
  class GetTaskByIdTests {

    @Test
    @WithMockUser(username = "creator@gmail.com")
    void shouldReturn200AndTask() throws Exception {
      when(taskService.getTaskById(taskId)).thenReturn(taskDto);

      mockMvc.perform(get("/api/v2/tasks/{id}", taskId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.project.id", is(1)))
          .andExpect(jsonPath("$.title", is("Fix the bug in the login module")))
          .andExpect(jsonPath("$.taskStatus", is("APPROVED")))
          .andExpect(jsonPath("$.priority", is("CRITICAL")))
          .andExpect(jsonPath("$.assignedTo.id", is(2)))
          .andExpect(jsonPath("$.assignedBy.id", is(1)));

      verify(taskService).getTaskById(taskId);
      verifyNoMoreInteractions(taskService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskService.getTaskById(taskId)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/tasks/{id}", taskId))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).getTaskById(taskId);
      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Update Task Tests")
  class UpdateTaskTests {

    @Test
    void shouldReturn200AndUpdatedTask() throws Exception {
      // Mocked response with updated title and description
      taskDto = new TaskDto(
          1L,
          new ProjectDto(1L, "Project Alpha", "This is a description of Project Alpha", senderDto),
          null,
          "Updated Task Title",
          "Updated task description with more details",
          now(),
          now().plusDays(5),
          now().plusDays(3),
          APPROVED,
          CRITICAL,
          receiverDto,
          senderDto,
          null
      );

      when(taskService.updateTask(any(TaskDto.class), eq(taskId))).thenReturn(taskDto);

      mockMvc.perform(put("/api/v2/tasks/{id}", taskId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
              {
                "project": {
                  "name": "Project Alpha",
                  "description": "This is a description of Project Alpha",
                  "creator": {
                    "fullName": "Duke Johnson",
                    "username": "sender@gmail.com",
                    "slug": "duke-johnson-1234"
                  }
                },
                "team": {
                  "name": "Team Alpha",
                  "description": "This is a description of Team A",
                  "creator": {
                    "fullName": "Duke Johnson",
                    "username": "sender@gmail.com",
                    "slug": "duke-johnson-1234"
                  }
                },
                "title": "Updated Task Title",
                "description": "Updated task description with more details",
                "expirationDate": "2025-07-01T16:00:00",
                "taskStatus": "APPROVED",
                "priority": "CRITICAL",
                "assignedTo": {
                  "fullName": "Alice Johnson",
                  "username": "receiver@gmail.com",
                  "slug": "alice-johnson-1234"
                },
                "assignedBy": {
                  "fullName": "Duke Johnson",
                  "username": "sender@gmail.com",
                  "slug": "duke-johnson-1234"
                }
              }
            """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.project.name", is("Project Alpha")))
          .andExpect(jsonPath("$.title", is("Updated Task Title")))
          .andExpect(jsonPath("$.description", is("Updated task description with more details")))
          .andExpect(jsonPath("$.taskStatus", is("APPROVED")))
          .andExpect(jsonPath("$.priority", is("CRITICAL")))
          .andExpect(jsonPath("$.assignedTo.username", is("receiver@gmail.com")))
          .andExpect(jsonPath("$.assignedBy.username", is("sender@gmail.com")));

      verify(taskService).updateTask(any(TaskDto.class), eq(taskId));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn400ForInvalidTaskDto() throws Exception {

      mockMvc.perform(put("/api/v2/tasks/{id}", taskId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON)
          .content("""
              {
                "project": { "id": 1 },
                "title": "",
                "description": "Updated Description",
                "createdAt": "2025-06-26T16:00:00",
                "dueDate": "2025-07-01T16:00:00",
                "updatedAt": "2025-06-29T16:00:00",
                "status": "APPROVED",
                "priority": "CRITICAL",
                "assignee": { "id": 2 },
                "creator": { "id": 1 }
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Get Soon Expiring Tasks Tests")
  class GetSoonExpiringTasksTests {

    @Test
    @WithMockUser(username = "creator@gmail.com")
    void shouldReturn200AndTasksList() throws Exception {
      when(taskService.findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName))).thenReturn(singletonList(taskDto));

      mockMvc.perform(get("/api/v2/tasks/expiring-soon")
          .param("username", username)
          .param("duration", "PT3H")
          .param("projectName", projectName)
          .param("teamName", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].id", is(1)))
          .andExpect(jsonPath("$[0].project.id", is(1)))
          .andExpect(jsonPath("$[0].title", is("Fix the bug in the login module")))
          .andExpect(jsonPath("$[0].taskStatus", is("APPROVED")))
          .andExpect(jsonPath("$[0].priority", is("CRITICAL")))
          .andExpect(jsonPath("$[0].assignedTo.id", is(2)))
          .andExpect(jsonPath("$[0].assignedBy.id", is(1)));

      verify(taskService).findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com")
    void shouldReturn200AndEmptyList() throws Exception {
      when(taskService.findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName))).thenReturn(emptyList());

      mockMvc.perform(get("/api/v2/tasks/expiring-soon")
          .param("username", username)
          .param("duration", "PT3H")
          .param("projectName", projectName)
          .param("teamName", teamName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(taskService).findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    @WithMockUser(username = "creator@gmail.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskService.findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName))).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/tasks/expiring-soon")
          .param("username", username)
          .param("duration", "PT3H")
          .param("projectName", projectName)
          .param("teamName", teamName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).findAllSoonExpiringTasks(eq(username), eq(duration), eq(projectName), eq(teamName));
      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Delete Task Tests")
  class DeleteTaskTests {

    @Test
    void shouldReturn204WhenTaskDeleted() throws Exception {
      doNothing().when(taskService).deleteTaskById(taskId);

      mockMvc.perform(delete("/api/v2/tasks/{id}", taskId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(taskService).deleteTaskById(taskId);
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(taskService).deleteTaskById(taskId);

      mockMvc.perform(delete("/api/v2/tasks/{id}", taskId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).deleteTaskById(taskId);
      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Upload Task Image Tests")
  class UploadTaskImageTests {

    @Test
    void shouldReturn201WhenImageUploaded() throws Exception {
      doNothing().when(taskService).uploadImage(eq(taskId), any(TaskImageDto.class));
      MockMultipartFile mockFile = new MockMultipartFile("file", "image.png", IMAGE_PNG_VALUE, "dummy image content".getBytes());

      mockMvc.perform(multipart("/api/v2/tasks/{id}/image", taskId)
          .file(mockFile)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(content().string(""));

      verify(taskService).uploadImage(eq(taskId), any(TaskImageDto.class));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn400ForInvalidImageDto() throws Exception {
      mockMvc.perform(post("/api/v2/tasks/{id}/image", taskId)
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

      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Update Task Image Tests")
  class UpdateTaskImageTests {

    @Test
    void shouldReturn200WhenImageUpdated() throws Exception {
      doNothing().when(taskService).updateImage(eq(taskId), any(TaskImageDto.class), eq(imageName));

      MockMultipartFile mockFile = new MockMultipartFile("file", "image.png", IMAGE_PNG_VALUE, "dummy image content".getBytes());

      mockMvc.perform(multipart("/api/v2/tasks/{id}/image/{imageName}", taskId, imageName)
          .file(mockFile)
          .with(request -> {
            request.setMethod("PUT");
            return request;
          })
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(content().string(""));

      verify(taskService).updateImage(eq(taskId), any(TaskImageDto.class), eq(imageName));
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn400ForInvalidImageDto() throws Exception {
      mockMvc.perform(put("/api/v2/tasks/{id}/image/{imageName}", taskId, imageName)
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

      verifyNoMoreInteractions(taskService);
    }
  }

  @Nested
  @DisplayName("Delete Task Image Tests")
  class DeleteTaskImageTests {

    @Test
    void shouldReturn204WhenImageDeleted() throws Exception {
      doNothing().when(taskService).deleteImage(taskId, imageName);

      mockMvc.perform(delete("/api/v2/tasks/{id}/image/{imageName}", taskId, imageName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(taskService).deleteImage(taskId, imageName);
      verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(taskService).deleteImage(taskId, imageName);

      mockMvc.perform(delete("/api/v2/tasks/{id}/image/{imageName}", taskId, imageName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(taskService).deleteImage(taskId, imageName);
      verifyNoMoreInteractions(taskService);
    }
  }
}