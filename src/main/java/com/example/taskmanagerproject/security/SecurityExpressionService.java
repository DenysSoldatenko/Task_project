package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.users.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.users.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.users.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.users.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.users.RoleName.TEAM_LEAD;
import static java.util.Arrays.asList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.RoleName;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.services.TaskCommentService;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.TeamUserService;
import com.example.taskmanagerproject.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for security expressions related to user and task access.
 */
@Slf4j
@RequiredArgsConstructor
@Service("expressionService")
public class SecurityExpressionService {

  private final UserService userService;
  private final TeamService teamService;
  private final TeamUserService teamUserService;
  private final TaskCommentService taskCommentService;

  private static final List<RoleName> ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS = asList(
      ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD
  );

  /**
   * Checks if the current user can access the specified user's data by slug.
   *
   * @param slug The slug of the user to check.
   * @return true if the current user matches the user's slug, false otherwise.
   */
  public boolean canAccessUserDataBySlug(String slug) {
    JwtEntity jwtEntity = (JwtEntity) getContext().getAuthentication().getPrincipal();
    UserDto user = userService.getUserBySlug(slug);
    boolean hasAccess = jwtEntity.getId().equals(user.id());
    log.info("Checking access for user slug: {} - hasAccess: {}", slug, hasAccess);
    return hasAccess;
  }

  /**
   * Checks if the current user has the necessary permissions to access a project.
   * The user must either be the creator of the project.
   *
   * @param projectName the name of the project to check access for
   * @return true if the user has access to the project, false otherwise
   */
  public boolean canAccessProject(String projectName) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isProjectCreator = userService.isProjectCreator(projectName, user.getUsername());
    log.info(
        "Checking access for user ID: {} on project: {} - hasAccess: {}, isProjectCreator: {}",
        user.getId(), projectName, isProjectCreator, isProjectCreator
    );
    return isProjectCreator;
  }

  /**
   * Checks if the current user has the necessary permissions to access a team.
   * The user must either be the creator of the team or be a member of the team.
   *
   * @param teamName the name of the team to check access for
   * @return true if the user has access to the project, false otherwise
   */
  public boolean canAccessTeam(String teamName) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean hasTeamAccess = userService.hasTeamAccess(teamName, user.getUsername());
    log.info(
        "Checking access for user ID: {} on team: {} - hasAccess: {}, hasTeamAccess: {}",
        user.getId(), teamName, hasTeamAccess, hasTeamAccess
    );
    return hasTeamAccess;
  }

  /**
   * Checks if the current user can access the specified task's data.
   *
   * @param taskId The ID of the task to check access for.
   * @return true if the current user can access the specified task's data, false otherwise.
   */
  public boolean canAccessTask(Long taskId) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isTaskOwner = userService.isUserTaskOwner(user.getId(), taskId);
    boolean isTaskAssignedToUser = userService.isUserAssignedToTask(user.getId(), taskId);
    boolean hasAccess = isTaskOwner || isTaskAssignedToUser;
    log.info(
        "Checking task access for task ID: {} - hasAccess: {}, isTaskOwner: {}, isTaskAssignedToUser: {}",
        taskId, hasAccess, isTaskOwner, isTaskAssignedToUser
    );
    return hasAccess;
  }

  /**
   * Checks if the current user can access the task data associated with the specified task comment slug.
   *
   * @param slug The slug of the task comment for which access is to be checked.
   * @return true if the current user can access the task data associated with the given slug, false otherwise.
   */
  public boolean canAccessTaskComment(String slug) {
    Long taskId = taskCommentService.getTaskIdBySlug(slug);
    return canAccessTask(taskId);
  }

  /**
   * Checks if the current user can access the task data associated with the specified task comment ID.
   *
   * @param taskCommentId The ID of the task comment for which access is to be checked.
   * @return true if the current user can access the task data associated with the given task comment ID, false otherwise.
   */
  public boolean canAccessTaskComment(Long taskCommentId) {
    Long taskId = taskCommentService.getTaskIdByTaskCommentId(taskCommentId);
    return canAccessTask(taskId);
  }

  /**
   * Checks if the current user can access the specified report.
   *
   * @param username The username of the user requesting the report.
   * @param teamName The associated team name.
   * @return true if the user has access, false otherwise.
   */
  public boolean canAccessReport(String username, String teamName) {
    JwtEntity jwt = (JwtEntity) getContext().getAuthentication().getPrincipal();
    User user = userService.getUserByUsername(username);
    return hasAccess(jwt, teamName, user.getId());
  }

  /**
   * Checks if the current user can access the specified report.
   *
   * @param teamName The associated team name.
   * @return true if the user has access, false otherwise.
   */
  public boolean canAccessReport(String teamName) {
    JwtEntity jwt = (JwtEntity) getContext().getAuthentication().getPrincipal();
    return hasAccess(jwt, teamName, jwt.getId());
  }

  private boolean hasAccess(JwtEntity jwt, String teamName, Long userId) {
    TeamDto team = teamService.getTeamByName(teamName);
    Role userRole = teamUserService.getRoleByTeamNameAndUsername(team.name(), jwt.getUsername());

    boolean hasPermission = ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS.stream()
        .anyMatch(role -> role.name().equals(userRole.getName()));
    boolean isUserInTeam = teamUserService.existsByUserIdAndTeamId(jwt.getId(), team.id());
    boolean canAccess = (jwt.getId().equals(userId) || hasPermission) && isUserInTeam;

    log.info(
        "Access check for report - Team: {}, User: {}, Role: {}, Has permission: {}, In team: {}, Access granted: {}",
        teamName, jwt.getUsername(), userRole.getName(), hasPermission, isUserInTeam, canAccess
    );

    return canAccess;
  }
}
