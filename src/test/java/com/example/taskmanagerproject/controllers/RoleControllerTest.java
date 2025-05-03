package com.example.taskmanagerproject.controllers;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.dtos.roles.RoleHierarchyListDto;
import com.example.taskmanagerproject.services.RoleService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link RoleController}, providing complete line coverage for all role-related endpoints.
 *
 * <p>These tests verify the behavior of the role management API, which includes creation, updating, deletion,
 * retrieval, and hierarchy management of user roles in the system. All service dependencies are mocked to ensure
 * isolated controller testing.
 *
 * <p>Functional areas tested:
 * <ul>
 *   <li>Fetching all roles and specific roles by name</li>
 *   <li>Creating and updating roles with role-based access control</li>
 *   <li>Deleting roles with proper authorization</li>
 *   <li>Managing role hierarchies (create, delete, and retrieve)</li>
 *   <li>Handling valid and invalid input payloads</li>
 *   <li>Handling internal service failures and exceptions</li>
 * </ul>
 *
 * <p>Test coverage includes status code assertions, response body content validation, and interaction verification
 * with the {@code RoleService}. Authentication is simulated via {@code @WithMockUser} and JWT-based request post-processors.
 *
 * <p><strong>Limitations and Known Gaps:</strong></p>
 * <ul>
 *   <li>Validation edge cases for malformed or null input DTOs are minimally covered</li>
 *   <li>Authorization failures (e.g., missing roles, insufficient authorities) are not tested explicitly</li>
 *   <li>No tests assert logging behavior or audit trail generation (if applicable)</li>
 *   <li>Domain-specific constraints (e.g., uniqueness violations, role hierarchy cycles) are not simulated</li>
 *   <li>Error responses for missing roles (e.g., 404 Not Found) are not explicitly validated</li>
 * </ul>
 *
 * <p>For greater resilience, consider augmenting this suite with:
 * <ul>
 *   <li>Dedicated unit tests for edge-case validation and business exceptions</li>
 *   <li>Security tests targeting unauthorized or forbidden access paths</li>
 *   <li>Contract or integration tests across service boundaries if applicable</li>
 * </ul>
 *
 * <p>This suite achieves controller-level validation with mock service boundaries and should be complemented
 * by deeper service-layer and security-focused tests for full stack assurance.</p>
 */
@WebMvcTest(controllers = RoleController.class)
class RoleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private RoleService roleService;

  private Jwt validJwt;
  private String roleName;
  private RoleDto roleDto;
  private RoleHierarchyListDto roleHierarchyListDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();
    roleName = "USER";
    roleDto = new RoleDto(1L, roleName, "User role");
    RoleDto higherDto = new RoleDto(100L, "ADMIN", "Description");
    RoleDto lowerDto = new RoleDto(100L, "DEVELOPER", "Description");
    roleHierarchyListDto = new RoleHierarchyListDto(roleName, List.of(higherDto), List.of(lowerDto));
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
  @DisplayName("Get All Roles Tests")
  class GetAllRolesTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndRoles() throws Exception {
      when(roleService.getAllRoles()).thenReturn(List.of(roleDto));

      mockMvc.perform(get("/api/v2/roles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].id", is(1)))
          .andExpect(jsonPath("$[0].name", is("USER")))
          .andExpect(jsonPath("$[0].description", is("User role")));

      verify(roleService).getAllRoles();
      verifyNoMoreInteractions(roleService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndEmptyList() throws Exception {
      when(roleService.getAllRoles()).thenReturn(emptyList());

      mockMvc.perform(get("/api/v2/roles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());

      verify(roleService).getAllRoles();
      verifyNoMoreInteractions(roleService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.getAllRoles()).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/roles"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).getAllRoles();
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Get Role By Name Tests")
  class GetRoleByNameTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndRole() throws Exception {
      when(roleService.getRoleByName(roleName)).thenReturn(roleDto);

      mockMvc.perform(get("/api/v2/roles/{roleName}", roleName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("USER")))
          .andExpect(jsonPath("$.description", is("User role")));

      verify(roleService).getRoleByName(roleName);
      verifyNoMoreInteractions(roleService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.getRoleByName(roleName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/roles/{roleName}", roleName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).getRoleByName(roleName);
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Create Role Tests")
  class CreateRoleTests {

    @Test
    void shouldReturn201AndCreatedRole() throws Exception {
      when(roleService.createRole(any(RoleDto.class))).thenReturn(roleDto);

      mockMvc.perform(post("/api/v2/roles")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"USER\",\"description\":\"User role\"}"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("USER")))
          .andExpect(jsonPath("$.description", is("User role")));

      verify(roleService).createRole(any(RoleDto.class));
      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn400ForInvalidRoleDto() throws Exception {
      mockMvc.perform(post("/api/v2/roles")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"\",\"description\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.createRole(any(RoleDto.class))).thenThrow(new RuntimeException("Creation failed"));

      mockMvc.perform(post("/api/v2/roles")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"USER\",\"description\":\"User role\"}"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Creation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).createRole(any(RoleDto.class));
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Update Role Tests")
  class UpdateRoleTests {

    @Test
    void shouldReturn200AndUpdatedRole() throws Exception {
      when(roleService.updateRole(eq(roleName), any(RoleDto.class))).thenReturn(roleDto);

      mockMvc.perform(put("/api/v2/roles/{roleName}", roleName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"USER\",\"description\":\"Updated user role\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(1)))
          .andExpect(jsonPath("$.name", is("USER")))
          .andExpect(jsonPath("$.description", is("User role")));

      verify(roleService).updateRole(eq(roleName), any(RoleDto.class));
      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn400ForInvalidRoleDto() throws Exception {
      mockMvc.perform(put("/api/v2/roles/{roleName}", roleName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"\",\"description\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.status", is("400")))
          .andExpect(jsonPath("$.error", is("Bad Request")));

      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.updateRole(eq(roleName), any(RoleDto.class))).thenThrow(new RuntimeException("Update failed"));

      mockMvc.perform(put("/api/v2/roles/{roleName}", roleName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"USER\",\"description\":\"Updated user role\"}"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Update failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).updateRole(eq(roleName), any(RoleDto.class));
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Delete Role Tests")
  class DeleteRoleTests {

    @Test
    void shouldReturn204WhenRoleDeleted() throws Exception {
      doNothing().when(roleService).deleteRole(roleName);

      mockMvc.perform(delete("/api/v2/roles/{roleName}", roleName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(roleService).deleteRole(roleName);
      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(roleService).deleteRole(roleName);

      mockMvc.perform(delete("/api/v2/roles/{roleName}", roleName)
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).deleteRole(roleName);
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Create Role Hierarchies Tests")
  class CreateRoleHierarchiesTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn201AndHierarchy() throws Exception {
      RoleDto higherRole = new RoleDto(1L, "ADMIN", "Has full access");
      RoleDto lowerRole = new RoleDto(2L, "DEVELOPER", "Writes code");

      RoleHierarchyListDto roleHierarchyListDto = new RoleHierarchyListDto("ADMIN", List.of(higherRole), List.of(lowerRole));
      when(roleService.findRoleWithHierarchy(roleName)).thenReturn(roleHierarchyListDto);

      mockMvc.perform(get("/api/v2/roles/{roleName}/hierarchy", roleName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.higherRoles[0].name", is("ADMIN")))
          .andExpect(jsonPath("$.higherRoles[0].description", is("Has full access")))
          .andExpect(jsonPath("$.lowerRoles[0].name", is("DEVELOPER")))
          .andExpect(jsonPath("$.lowerRoles[0].description", is("Writes code")));

      verify(roleService).findRoleWithHierarchy(roleName);
      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.createRoleHierarchies(any())).thenThrow(new RuntimeException("Creation failed"));

      mockMvc.perform(post("/api/v2/roles/hierarchies")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("[{\"parentRoleName\":\"ADMIN\",\"childRoleName\":\"USER\"}]"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Creation failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).createRoleHierarchies(any());
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Delete Role Hierarchies Tests")
  class DeleteRoleHierarchiesTests {

    @Test
    void shouldReturn204WhenHierarchiesDeleted() throws Exception {
      doNothing().when(roleService).deleteRoleHierarchies(any());

      mockMvc.perform(delete("/api/v2/roles/hierarchies")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("[{\"parentRoleName\":\"ADMIN\",\"childRoleName\":\"USER\"}]"))
          .andExpect(status().isNoContent())
          .andExpect(content().string(""));

      verify(roleService).deleteRoleHierarchies(any());
      verifyNoMoreInteractions(roleService);
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
      doThrow(new RuntimeException("Deletion failed")).when(roleService).deleteRoleHierarchies(any());

      mockMvc.perform(delete("/api/v2/roles/hierarchies")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
          .contentType(MediaType.APPLICATION_JSON)
          .content("[{\"parentRoleName\":\"ADMIN\",\"childRoleName\":\"USER\"}]"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Deletion failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).deleteRoleHierarchies(any());
      verifyNoMoreInteractions(roleService);
    }
  }

  @Nested
  @DisplayName("Get Role Hierarchy Tests")
  class GetRoleHierarchyTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndHierarchy() throws Exception {
      when(roleService.findRoleWithHierarchy(roleName)).thenReturn(roleHierarchyListDto);

      mockMvc.perform(get("/api/v2/roles/{roleName}/hierarchy", roleName))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.higherRoles[0].name", is("ADMIN")))
          .andExpect(jsonPath("$.lowerRoles[0].name", is("DEVELOPER")));

      verify(roleService).findRoleWithHierarchy(roleName);
      verifyNoMoreInteractions(roleService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500WhenServiceFails() throws Exception {
      when(roleService.findRoleWithHierarchy(roleName)).thenThrow(new RuntimeException("Retrieval failed"));

      mockMvc.perform(get("/api/v2/roles/{roleName}/hierarchy", roleName))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message", is("Retrieval failed")))
          .andExpect(jsonPath("$.status", is("500")))
          .andExpect(jsonPath("$.error", is("Internal Server Error")));

      verify(roleService).findRoleWithHierarchy(roleName);
      verifyNoMoreInteractions(roleService);
    }
  }
}