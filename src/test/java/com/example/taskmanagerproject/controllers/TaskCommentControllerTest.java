package com.example.taskmanagerproject.controllers;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
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
import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.services.TaskCommentService;
import java.time.Instant;
import java.util.Collections;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link TaskCommentController}, providing complete line coverage for all task comment-related endpoints.
 *
 * <p>These tests validate the behavior of the task comment API, including creation, updating, deletion,
 * and retrieval of comments associated with tasks. All dependencies on {@code TaskCommentService} are mocked
 * to ensure strict isolation of the controller layer during testing.
 *
 * <p>Functional areas tested:
 * <ul>
 *   <li>Creating new task comments with valid payloads</li>
 *   <li>Updating existing comments by ID</li>
 *   <li>Deleting comments with appropriate authorization</li>
 *   <li>Retrieving comments by task slug with pagination support</li>
 *   <li>Handling invalid inputs such as null task references</li>
 *   <li>Handling internal service exceptions (e.g., runtime failures)</li>
 * </ul>
 *
 * <p>Test coverage includes status code assertions, response body structure and value validation, and
 * interaction verification with the mocked {@code TaskCommentService}. JWT-based authentication is simulated
 * via Spring Security’s {@code jwt()} request post-processor and {@code @WithMockUser} annotations.
 *
 * <p><strong>Limitations and Known Gaps:</strong></p>
 * <ul>
 *   <li>Validation scenarios beyond null tasks (e.g., empty messages, invalid slugs) are not covered</li>
 *   <li>Authorization failures (e.g., access by users without proper roles) are not explicitly tested</li>
 *   <li>Domain-specific exceptions (e.g., comment not found, permission denied) are not simulated</li>
 *   <li>Malformed request bodies and JSON deserialization errors are not exercised</li>
 *   <li>Optimistic locking or concurrent update edge cases are not addressed</li>
 * </ul>
 *
 * <p>Recommendations for increased test resilience and breadth:
 * <ul>
 *   <li>Add security-focused tests for forbidden and unauthorized scenarios</li>
 *   <li>Expand edge-case validation coverage (e.g., field length, required fields, date constraints)</li>
 *   <li>Include tests for exception mapping of known domain errors</li>
 *   <li>Use parameterized tests for variations in payload structure and content</li>
 * </ul>
 *
 * <p>This suite provides solid controller-level coverage with mocked service boundaries and should be extended
 * with deeper unit and integration tests at the service and security layers to ensure full system robustness.</p>
 */
@WebMvcTest(controllers = TaskCommentController.class)
class TaskCommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private TaskCommentService taskCommentService;

  private String slug;
  private Jwt validJwt;
  private Long commentId;
  private TaskCommentDto taskCommentDto;
  private Page<TaskCommentDto> commentPage;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();

    UserDto senderDto = new UserDto(1L, "sender", "sender@gmail.com", "", "", null);
    UserDto receiverDto = new UserDto(2L, "receiver", "receiver@gmail.com", "", "", null);
    ProjectDto projectDto = new ProjectDto(1L, "P", "desc", senderDto);
    TaskDto taskDto = new TaskDto(1L, projectDto, null, "T", "D", now(), now().plusDays(5), now().plusDays(3), APPROVED, CRITICAL, receiverDto, senderDto, null);
    slug = "task-slug-1";
    commentId = 1L;
    taskCommentDto = new TaskCommentDto(commentId, taskDto, senderDto, receiverDto, slug, "Test comment", now());
    commentPage = new PageImpl<>(singletonList(taskCommentDto));
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
  @DisplayName("Create Task Comment Tests")
  class CreateTaskCommentTests {

    @Test
    void shouldReturn201AndCreatedComment() throws Exception {
      when(taskCommentService.createComment(any(TaskCommentDto.class))).thenReturn(taskCommentDto);

      mockMvc.perform(post("/api/v2/task-comments")
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": { "id": 1 },
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Test comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.task.id", is(1)))
          .andExpect(jsonPath("$.sender.id", is(1)))
          .andExpect(jsonPath("$.receiver.id", is(2)))
          .andExpect(jsonPath("$.slug", is("task-slug-1")))
          .andExpect(jsonPath("$.message", is("Test comment")));

      verify(taskCommentService).createComment(any(TaskCommentDto.class));
      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    void shouldReturn400ForNullTask() throws Exception {

      mockMvc.perform(post("/api/v2/task-comments")
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": null,
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Test comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));


      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskCommentService.createComment(any(TaskCommentDto.class))).thenThrow(new RuntimeException("Creation failed"));

      mockMvc.perform(post("/api/v2/task-comments")
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": { "id": 1 },
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Test comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Creation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));


      verify(taskCommentService).createComment(any(TaskCommentDto.class));
      verifyNoMoreInteractions(taskCommentService);
    }
  }

  @Nested
  @DisplayName("Update Task Comment Tests")
  class UpdateTaskCommentTests {

    @Test
    void shouldReturn200AndUpdatedComment() throws Exception {
      when(taskCommentService.updateTaskComment(any(TaskCommentDto.class), eq(commentId))).thenReturn(taskCommentDto);

      mockMvc.perform(put("/api/v2/task-comments/{id}", commentId)
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": { "id": 1 },
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Updated comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.task.id", is(1)))
          .andExpect(jsonPath("$.sender.id", is(1)))
          .andExpect(jsonPath("$.receiver.id", is(2)))
          .andExpect(jsonPath("$.slug", is("task-slug-1")))
          .andExpect(jsonPath("$.message", is("Test comment")));


      verify(taskCommentService).updateTaskComment(any(TaskCommentDto.class), eq(commentId));
      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    void shouldReturn400ForNullTask() throws Exception {

      mockMvc.perform(put("/api/v2/task-comments/{id}", commentId)
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": null,
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Updated comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));


      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskCommentService.updateTaskComment(any(TaskCommentDto.class), eq(commentId))).thenThrow(new RuntimeException("Update failed"));

      mockMvc.perform(put("/api/v2/task-comments/{id}", commentId)
            .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
            .contentType(APPLICATION_JSON)
            .content("""
              {
                "task": { "id": 1 },
                "sender": { "id": 1 },
                "receiver": { "id": 2 },
                "slug": "task-slug-1",
                "message": "Updated comment",
                "createdAt": "2025-06-26T13:49:00"
              }
            """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Update failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));


      verify(taskCommentService).updateTaskComment(any(TaskCommentDto.class), eq(commentId));
      verifyNoMoreInteractions(taskCommentService);
    }
  }

  @Nested
  @DisplayName("Delete Task Comment Tests")
  class DeleteTaskCommentTests {

    @Test
    void shouldReturn204WhenCommentDeleted() throws Exception {
      doNothing().when(taskCommentService).deleteTaskComment(commentId);

      mockMvc.perform(delete("/api/v2/task-comments/{id}", commentId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));


      verify(taskCommentService).deleteTaskComment(commentId);
      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(taskCommentService).deleteTaskComment(commentId);

      mockMvc.perform(delete("/api/v2/task-comments/{id}", commentId)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER")))
          .contentType(APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));


      verify(taskCommentService).deleteTaskComment(commentId);
      verifyNoMoreInteractions(taskCommentService);
    }
  }

  @Nested
  @DisplayName("Get Task Comments By Slug Tests")
  class GetTaskCommentsBySlugTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndCommentsPage() throws Exception {
      when(taskCommentService.getCommentsByTaskSlug(eq(slug), any(PageRequest.class))).thenReturn(commentPage);

      mockMvc.perform(get("/api/v2/task-comments/{slug}", slug)
          .param("page", "0")
          .param("size", "1")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id", is(1)))
          .andExpect(jsonPath("$.content[0].task.id", is(1)))
          .andExpect(jsonPath("$.content[0].sender.id", is(1)))
          .andExpect(jsonPath("$.content[0].receiver.id", is(2)))
          .andExpect(jsonPath("$.content[0].slug", is("task-slug-1")))
          .andExpect(jsonPath("$.content[0].message", is("Test comment")))
          .andExpect(jsonPath("$.number", is(0)))
          .andExpect(jsonPath("$.size", is(1)))
          .andExpect(jsonPath("$.totalElements", is(1)))
          .andExpect(jsonPath("$.content", hasSize(1)));

      verify(taskCommentService).getCommentsByTaskSlug(eq(slug), any(PageRequest.class));
      verifyNoMoreInteractions(taskCommentService);
    }


    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndEmptyPage() throws Exception {
      when(taskCommentService.getCommentsByTaskSlug(eq(slug), any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

      mockMvc.perform(get("/api/v2/task-comments/{slug}", slug)
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isEmpty())
          .andExpect(jsonPath("$.number", is(0)))
          .andExpect(jsonPath("$.size", is(0)))
          .andExpect(jsonPath("$.totalElements", is(0)))
          .andExpect(jsonPath("$.content", hasSize(0)));

      verify(taskCommentService).getCommentsByTaskSlug(eq(slug), any(PageRequest.class));
      verifyNoMoreInteractions(taskCommentService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(taskCommentService.getCommentsByTaskSlug(eq(slug), any(PageRequest.class))).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/task-comments/{slug}", slug)
          .param("page", "0")
          .param("size", "10")
          .param("sort", "id,asc"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));


      verify(taskCommentService).getCommentsByTaskSlug(eq(slug), any(PageRequest.class));
      verifyNoMoreInteractions(taskCommentService);
    }
  }
}