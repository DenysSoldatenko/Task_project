package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.roles.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.roles.RoleName.FULLSTACK_DEVELOPER;
import static com.example.taskmanagerproject.entities.roles.RoleName.JUNIOR_DEVELOPER;
import static com.example.taskmanagerproject.entities.roles.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.roles.RoleName.TEAM_LEAD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TaskCommentService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.TeamUserService;
import com.example.taskmanagerproject.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityExpressionServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private TeamService teamService;

  @Mock
  private ProjectService projectService;

  @Mock
  private TeamUserService teamUserService;

  @Mock
  private TaskCommentService taskCommentService;

  @InjectMocks
  private SecurityExpressionService service;

  private Jwt jwt;
  private SecurityContext securityContext;
  private JwtAuthenticationToken authToken;
  private MockedStatic<SecurityContextHolder> securityContextHolderMock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    securityContextHolderMock = mockStatic(SecurityContextHolder.class);
    securityContext = mock(SecurityContext.class);
    jwt = mock(Jwt.class);
    authToken = mock(JwtAuthenticationToken.class);

    when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authToken);
    when(authToken.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsString("email")).thenReturn("user@gmail.com");
  }

  @AfterEach
  void tearDown() {
    securityContextHolderMock.close();
  }

  @Test
  void shouldDenyAccessToOtherUserDataBySlug() {
    User currentUser = new User(1L, "user@gmail.com", "Current", "current-slug", null, null);
    UserDto targetUserDto = new UserDto(2L, "other@gmail.com", "Other", "other-slug", null, null);

    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    when(userService.getUserBySlug("other-slug")).thenReturn(targetUserDto);

    boolean result = service.canAccessUserDataBySlug("other-slug");
    assertFalse(result);

    verify(userService).getUserBySlug("other-slug");
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenJwtIsNullForUserData() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> service.canAccessUserDataBySlug("user-slug"));
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowProjectAccessForAuthorizedUser() {
    when(userService.hasProjectAccess("Test Project", "user@gmail.com")).thenReturn(true);

    boolean result = service.canAccessProject("Test Project");
    assertTrue(result);

    verify(userService).hasProjectAccess("Test Project", "user@gmail.com");
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyProjectAccessForUnauthorizedUser() {
    when(userService.hasProjectAccess("Test Project", "user@gmail.com")).thenReturn(false);

    boolean result = service.canAccessProject("Test Project");
    assertFalse(result);

    verify(userService).hasProjectAccess("Test Project", "user@gmail.com");
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenJwtIsNullForProject() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> service.canAccessProject("Test Project"));
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTeamAccessForAuthorizedUser() {
    when(userService.hasTeamAccess("Test Team", "user@gmail.com")).thenReturn(true);

    boolean result = service.canAccessTeam("Test Team");
    assertTrue(result);

    verify(userService).hasTeamAccess("Test Team", "user@gmail.com");
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyTeamAccessForUnauthorizedUser() {
    when(userService.hasTeamAccess("Test Team", "user@gmail.com")).thenReturn(false);

    boolean result = service.canAccessTeam("Test Team");
    assertFalse(result);

    verify(userService).hasTeamAccess("Test Team", "user@gmail.com");
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenJwtIsNullForTeam() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> service.canAccessTeam("Test Team"));
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTaskAccessForTaskOwner() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(true);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(false);

    boolean result = service.canAccessTask(1L);
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTaskAccessForAssignedUser() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(false);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(true);

    boolean result = service.canAccessTask(1L);
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyTaskAccessForUnauthorizedUser() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(false);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(false);

    boolean result = service.canAccessTask(1L);
    assertFalse(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenJwtIsNullForTask() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> service.canAccessTask(1L));
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTaskCommentAccessBySlugForAuthorizedTask() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(taskCommentService.getTaskIdBySlug("task-comment-slug")).thenReturn(1L);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(true);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(true);

    boolean result = service.canAccessTaskComment("task-comment-slug");
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(taskCommentService).getTaskIdBySlug("task-comment-slug");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyTaskCommentAccessBySlugForUnauthorizedTask() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(taskCommentService.getTaskIdBySlug("task-comment-slug")).thenReturn(1L);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(false);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(false);

    boolean result = service.canAccessTaskComment("task-comment-slug");
    assertFalse(result);

    verify(taskCommentService).getTaskIdBySlug("task-comment-slug");
    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTaskCommentAccessByIdForAuthorizedTask() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);

    when(taskCommentService.getTaskIdByTaskCommentId(1L)).thenReturn(1L);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(true);

    boolean result = service.canAccessTaskComment(1L);
    assertTrue(result);

    verify(taskCommentService).getTaskIdByTaskCommentId(1L);
    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verifyNoMoreInteractions(teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyTaskCommentAccessByIdForUnauthorizedTask() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(taskCommentService.getTaskIdByTaskCommentId(1L)).thenReturn(1L);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    when(userService.isUserTaskOwner(1L, 1L)).thenReturn(false);
    when(userService.isUserAssignedToTask(1L, 1L)).thenReturn(false);

    boolean result = service.canAccessTaskComment(1L);
    assertFalse(result);

    verify(taskCommentService).getTaskIdByTaskCommentId(1L);
    verify(userService).getUserByUsername("user@gmail.com");
    verify(userService).isUserTaskOwner(1L, 1L);
    verify(userService).isUserAssignedToTask(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowUserReportAccessForSelf() {
    User user = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(user);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    Role role = new Role();
    role.setName(ADMIN.name());
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    when(teamUserService.existsByUserIdAndTeamId(1L, 1L)).thenReturn(true);

    boolean result = service.canAccessUserReport("user@gmail.com", "Test Team");
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowUserReportAccessForAdminRole() {
    User targetUser = new User(2L, "other@gmail.com", "Other", "other-slug", null, null);
    User currentUser = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("other@gmail.com")).thenReturn(targetUser);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    Role role = new Role();
    role.setName(ADMIN.name());
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    when(teamUserService.existsByUserIdAndTeamId(2L, 1L)).thenReturn(true);

    boolean result = service.canAccessUserReport("other@gmail.com", "Test Team");
    assertTrue(result);

    verify(userService).getUserByUsername("other@gmail.com");
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(2L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyUserReportAccessForNonRelatedUser() {
    User targetUser = new User(2L, "other@gmail.com", "Other", "other-slug", null, null);
    User currentUser = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    when(userService.getUserByUsername("other@gmail.com")).thenReturn(targetUser);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    Role role = new Role();
    role.setName(FULLSTACK_DEVELOPER.name());
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    when(teamUserService.existsByUserIdAndTeamId(2L, 1L)).thenReturn(false);

    boolean result = service.canAccessUserReport("other@gmail.com", "Test Team");
    assertFalse(result);

    verify(userService).getUserByUsername("other@gmail.com");
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(2L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowTeamReportAccessForAdminRole() {
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    Role role = new Role();
    role.setName(ADMIN.name());
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    when(teamUserService.existsByUserIdAndTeamId(1L, 1L)).thenReturn(true);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(new User(1L, "user@gmail.com", "User", "user-slug", null, null));

    boolean result = service.canAccessTeamReport("Test Team");
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyTeamReportAccessForNonAuthorizedRole() {
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    Role role = new Role();
    role.setName(FULLSTACK_DEVELOPER.name());
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(role);
    when(teamUserService.existsByUserIdAndTeamId(1L, 1L)).thenReturn(true);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(new User(1L, "user@gmail.com", "User", "user-slug", null, null));

    boolean result = service.canAccessTeamReport("Test Team");
    assertFalse(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowProjectReportAccessForManagerRole() {
    ProjectDto project = new ProjectDto(1L, "Test Project", "Description", null);
    when(projectService.getProjectByName("Test Project")).thenReturn(project);
    Role role = new Role();
    role.setName(MANAGER.name());
    when(projectService.getRoleByProjectNameAndUsername("Test Project", "user@gmail.com")).thenReturn(role);
    when(projectService.existsByUserIdAndProjectId(1L, 1L)).thenReturn(true);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(new User(1L, "user@gmail.com", "User", "user-slug", null, null));

    boolean result = service.canAccessProjectReport("Test Project");
    assertTrue(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(projectService).getProjectByName("Test Project");
    verify(projectService).getRoleByProjectNameAndUsername("Test Project", "user@gmail.com");
    verify(projectService).existsByUserIdAndProjectId(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyProjectReportAccessForNonRelatedUser() {
    ProjectDto project = new ProjectDto(1L, "Test Project", "Description", null);
    when(projectService.getProjectByName("Test Project")).thenReturn(project);
    Role role = new Role();
    role.setName(JUNIOR_DEVELOPER.name());
    when(projectService.getRoleByProjectNameAndUsername("Test Project", "user@gmail.com")).thenReturn(role);
    when(projectService.existsByUserIdAndProjectId(1L, 1L)).thenReturn(false);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(new User(1L, "user@gmail.com", "User", "user-slug", null, null));

    boolean result = service.canAccessProjectReport("Test Project");
    assertFalse(result);

    verify(userService).getUserByUsername("user@gmail.com");
    verify(projectService).getProjectByName("Test Project");
    verify(projectService).getRoleByProjectNameAndUsername("Test Project", "user@gmail.com");
    verify(projectService).existsByUserIdAndProjectId(1L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldAllowExpiringTasksAccessForAuthorizedUser() {
    Role projectRole = new Role();
    projectRole.setName(ADMIN.name());
    Role teamRole = new Role();
    teamRole.setName(TEAM_LEAD.name());

    User targetUser = new User(2L, "other@gmail.com", "Other", "other-slug", null, null);
    User currentUser = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    ProjectDto project = new ProjectDto(1L, "Test Project", "Description", null);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);

    when(userService.getUserByUsername("other@gmail.com")).thenReturn(targetUser);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    when(projectService.getProjectByName("Test Project")).thenReturn(project);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    when(projectService.getRoleByProjectNameAndUsername("Test Project", "user@gmail.com")).thenReturn(projectRole);
    when(projectService.existsByUserIdAndProjectId(2L, 1L)).thenReturn(true);
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(teamRole);
    when(teamUserService.existsByUserIdAndTeamId(2L, 1L)).thenReturn(true);

    boolean result = service.canAccessExpiringTasks("other@gmail.com", "Test Project", "Test Team");
    assertTrue(result);

    verify(userService, times(2)).getUserByUsername("other@gmail.com");
    verify(projectService).getProjectByName("Test Project");
    verify(projectService).getRoleByProjectNameAndUsername("Test Project", "user@gmail.com");
    verify(projectService).existsByUserIdAndProjectId(2L, 1L);
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(2L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyExpiringTasksAccessForUnauthorizedProject() {
    Role teamRole = new Role();
    teamRole.setName(TEAM_LEAD.name());

    User targetUser = new User(2L, "other@gmail.com", "Other", "other-slug", null, null);
    User currentUser = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    ProjectDto project = new ProjectDto(1L, "Test Project", "Description", null);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);

    when(userService.getUserByUsername("other@gmail.com")).thenReturn(targetUser);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    when(projectService.getProjectByName("Test Project")).thenReturn(project);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    when(projectService.getRoleByProjectNameAndUsername("Test Project", "user@gmail.com")).thenReturn(new Role());
    when(projectService.existsByUserIdAndProjectId(2L, 1L)).thenReturn(false);
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(teamRole);
    when(teamUserService.existsByUserIdAndTeamId(2L, 1L)).thenReturn(true);

    boolean result = service.canAccessExpiringTasks("other@gmail.com", "Test Project", "Test Team");
    assertFalse(result);

    verify(userService, times(2)).getUserByUsername("other@gmail.com");
    verify(projectService).getProjectByName("Test Project");
    verify(projectService).getRoleByProjectNameAndUsername("Test Project", "user@gmail.com");
    verify(projectService).existsByUserIdAndProjectId(2L, 1L);
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(2L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldDenyExpiringTasksAccessForUnauthorizedTeam() {
    Role projectRole = new Role();
    projectRole.setName(ADMIN.name());

    User targetUser = new User(2L, "other@gmail.com", "Other", "other-slug", null, null);
    User currentUser = new User(1L, "user@gmail.com", "User", "user-slug", null, null);
    ProjectDto project = new ProjectDto(1L, "Test Project", "Description", null);
    TeamDto team = new TeamDto(1L, "Test Team", "Description", null);

    when(userService.getUserByUsername("other@gmail.com")).thenReturn(targetUser);
    when(userService.getUserByUsername("user@gmail.com")).thenReturn(currentUser);
    when(projectService.getProjectByName("Test Project")).thenReturn(project);
    when(teamService.getTeamByName("Test Team")).thenReturn(team);
    when(projectService.getRoleByProjectNameAndUsername("Test Project", "user@gmail.com")).thenReturn(projectRole);
    when(projectService.existsByUserIdAndProjectId(2L, 1L)).thenReturn(true);
    when(teamUserService.getRoleByTeamNameAndUsername("Test Team", "user@gmail.com")).thenReturn(new Role());
    when(teamUserService.existsByUserIdAndTeamId(2L, 1L)).thenReturn(false);

    boolean result = service.canAccessExpiringTasks("other@gmail.com", "Test Project", "Test Team");
    assertFalse(result);

    verify(userService, times(2)).getUserByUsername("other@gmail.com");
    verify(projectService).getProjectByName("Test Project");
    verify(projectService).getRoleByProjectNameAndUsername("Test Project", "user@gmail.com");
    verify(projectService).existsByUserIdAndProjectId(2L, 1L);
    verify(teamService).getTeamByName("Test Team");
    verify(teamUserService).getRoleByTeamNameAndUsername("Test Team", "user@gmail.com");
    verify(teamUserService).existsByUserIdAndTeamId(2L, 1L);
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenJwtIsNullForExpiringTasks() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(NullPointerException.class, () -> service.canAccessExpiringTasks("other@gmail.com", "Test Project", "Test Team"));
    verifyNoMoreInteractions(userService, teamService, projectService, teamUserService, taskCommentService);
  }
}