package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.users.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.users.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.users.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.users.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.users.RoleName.TEAM_LEAD;
import static java.util.Arrays.asList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.RoleName;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.services.TaskCommentService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.TeamUserService;
import com.example.taskmanagerproject.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating user permissions and access rights across users, teams, tasks, and projects.
 * Used in Spring Security expressions (for example `@PreAuthorize`).
 */
@Slf4j
@RequiredArgsConstructor
@Service("expressionService")
public class SecurityExpressionService {

  private final UserService userService;
  private final TeamService teamService;
  private final ProjectService projectService;
  private final TeamUserService teamUserService;
  private final TaskCommentService taskCommentService;

  private static final List<RoleName> ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS = asList(
      ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD
  );

  /**
   * Checks if the current user can access data of a user identified by slug.
   */
  public boolean canAccessUserDataBySlug(String slug) {
    Jwt jwt = getJwt();
    UserDto user = userService.getUserBySlug(slug);
    boolean hasAccess = jwt.getClaimAsString("email").equals(user.username());
    log.info("Checking access for user slug: {} - hasAccess: {}", slug, hasAccess);
    return hasAccess;
  }

  /**
   * Checks if the current user has access to the specified project.
   */
  public boolean canAccessProject(String projectName) {
    Jwt jwt = getJwt();
    String email = jwt.getClaimAsString("email");
    boolean hasProjectAccess = userService.hasProjectAccess(projectName, email);
    log.info("Checking access for user email: {} on project: {} - hasAccess: {}", email, projectName, hasProjectAccess);
    return hasProjectAccess;
  }

  /**
   * Checks if the current user has access to the specified team.
   */
  public boolean canAccessTeam(String teamName) {
    Jwt jwt = getJwt();
    String email = jwt.getClaimAsString("email");
    boolean hasTeamAccess = userService.hasTeamAccess(teamName, email);
    log.info("Checking access for user email: {} on team: {} - hasAccess: {}", email, teamName, hasTeamAccess);
    return hasTeamAccess;
  }

  /**
   * Checks if the current user is the owner of or assigned to the given task.
   */
  public boolean canAccessTask(Long taskId) {
    Jwt jwt = getJwt();
    String email = jwt.getClaimAsString("email");
    Long userId = userService.getUserByUsername(email).getId();
    boolean isTaskOwner = userService.isUserTaskOwner(userId, taskId);
    boolean isTaskAssignedToUser = userService.isUserAssignedToTask(userId, taskId);
    boolean hasAccess = isTaskOwner || isTaskAssignedToUser;
    log.info("Checking task access for user email: {}, task ID: {}, hasAccess: {}", email, taskId, hasAccess);
    return hasAccess;
  }

  /**
   * Checks access to a task comment using its slug.
   */
  public boolean canAccessTaskComment(String slug) {
    Long taskId = taskCommentService.getTaskIdBySlug(slug);
    return canAccessTask(taskId);
  }

  /**
   * Checks access to a task comment using its ID.
   */
  public boolean canAccessTaskComment(Long taskCommentId) {
    Long taskId = taskCommentService.getTaskIdByTaskCommentId(taskCommentId);
    return canAccessTask(taskId);
  }

  /**
   * Checks if a user can access a report related to another user in a team.
   */
  public boolean canAccessUserReport(String username, String teamName) {
    return hasAccess(username, teamName, false);
  }

  /**
   * Checks if a user can access a report related to a team.
   */
  public boolean canAccessTeamReport(String teamName) {
    return hasAccess(null, teamName, false);
  }

  /**
   * Checks if a user can access a report related to a project.
   */
  public boolean canAccessProjectReport(String projectName) {
    return hasAccess(null, projectName, true);
  }

  /**
   * Determines whether the specified user has access to view expiring tasks
   * by verifying their access to both the given project and team.
   *
   * @param username    the username of the user whose access is being verified
   * @param projectName the name of the project associated with the expiring tasks
   * @param teamName    the name of the team responsible for the tasks
   * @return {@code true} if the user has access to both the specified project and team; {@code false} otherwise
   */
  public boolean canAccessExpiringTasks(String username, String projectName, String teamName) {
    Jwt jwt = getJwt();
    String email = jwt.getClaimAsString("email");
    boolean hasProjectAccess = hasAccess(username, projectName, true);
    boolean hasTeamAccess = hasAccess(username, teamName, false);
    boolean canAccess = hasProjectAccess && hasTeamAccess;
    log.info("Access check for user='{}', project='{}', team='{}', granted={}", email, projectName, teamName, canAccess);
    return canAccess;
  }

  /**
   * Core permission check logic based on user-role and relation to the entity.
   */
  private boolean hasAccess(String username, String entityName, boolean isProject) {
    Jwt jwt = getJwt();
    String email = jwt.getClaimAsString("email");
    Long userId = (username != null) ? userService.getUserByUsername(username).getId() : userService.getUserByUsername(email).getId();

    Role userRole;
    boolean isUserRelated;

    if (isProject) {
      ProjectDto project = projectService.getProjectByName(entityName);
      userRole = projectService.getRoleByProjectNameAndUsername(entityName, email);
      isUserRelated = projectService.existsByUserIdAndProjectId(userId, project.id());
    } else {
      TeamDto team = teamService.getTeamByName(entityName);
      userRole = teamUserService.getRoleByTeamNameAndUsername(team.name(), email);
      isUserRelated = teamUserService.existsByUserIdAndTeamId(userId, team.id());
    }

    boolean hasPermission = ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS.stream().anyMatch(role -> role.name().equals(userRole.getName()));
    boolean canAccess = (username != null) ? (email.equals(username) || (hasPermission && isUserRelated)) : (hasPermission && isUserRelated);

    log.info(
        "Access check - Type: {}, Name: {}, User email: {}, Role: {}, Has permission: {}, Related: {}, Access granted: {}",
        isProject ? "Project" : "Team", entityName, email, userRole.getName(), hasPermission, isUserRelated, canAccess
    );

    return canAccess;
  }

  /**
   * Extracts the current user's JWT token from the security context.
   */
  private Jwt getJwt() {
    JwtAuthenticationToken authentication = (JwtAuthenticationToken) getContext().getAuthentication();
    return authentication.getToken();
  }
}
