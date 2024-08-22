package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.security.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.security.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.security.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.security.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.security.RoleName.TEAM_LEAD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.dtos.security.UserDto;
import com.example.taskmanagerproject.dtos.team.TeamDto;
import com.example.taskmanagerproject.entities.security.Role;
import com.example.taskmanagerproject.entities.security.RoleName;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.services.TeamUserService;
import com.example.taskmanagerproject.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

  private static final List<RoleName> ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS = asList(
      ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD
  );

  /**
   * Checks if the current user has the ADMIN role.
   *
   * @return true if the current user has the 'ADMIN' role, false otherwise.
   */
  public boolean hasAdminRole() {
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    log.info("Checking if user has ADMIN role - isAdmin: {}", isAdmin);
    return isAdmin;
  }

  /**
   * Checks if the current user can access the specified user's data by slug.
   *
   * @param slug The slug of the user to check.
   * @return true if the current user is an admin or matches the user's slug.
   */
  public boolean canAccessUserDataBySlug(String slug) {
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    JwtEntity jwtEntity = (JwtEntity) getContext().getAuthentication().getPrincipal();
    UserDto user = userService.getUserBySlug(slug);
    log.info("Checking access for user slug: {} - isAdmin: {}", slug, isAdmin);
    return jwtEntity.getId().equals(user.id()) || isAdmin;
  }

  /**
   * Checks if the current user can access the specified user's data by ID.
   *
   * @param id The ID of the user to check.
   * @return true if the current user is an admin or matches the user's ID.
   */
  public boolean canAccessUserDataById(Long id) {
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    log.info("Checking access for user ID: {} - isAdmin: {}", id, isAdmin);
    return user.getId().equals(id) || isAdmin;
  }

  /**
   * Checks if the current user has the necessary permissions to access a project.
   * The user must either be the creator of the project or have admin privileges.
   *
   * @param projectName the name of the project to check access for
   * @return true if the user has access to the project, false otherwise
   */
  public boolean canAccessProject(String projectName) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    boolean isProjectCreator = userService.isProjectCreator(projectName, user.getUsername());
    log.info(
        "Checking access for user ID: {} on project name: {} - isAdmin: {}, isProjectCreator: {}",
        user.getId(), projectName, isAdmin, isProjectCreator
    );
    return isAdmin || isProjectCreator;
  }

  /**
   * Checks if the current user has the necessary permissions to access a team.
   * The user must either be the creator of the team, have admin privileges, or be a member of the team.
   *
   * @param teamName the name of the team to check access for
   * @return true if the user has access to the project, false otherwise
   */
  public boolean canAccessTeam(String teamName) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    boolean hasTeamAccess = userService.hasTeamAccess(teamName, user.getUsername());
    log.info(
        "Checking access for user ID: {} on team name: {} - isAdmin: {}, hasTeamAccess: {}",
        user.getId(), teamName, isAdmin, hasTeamAccess
    );
    return isAdmin || hasTeamAccess;
  }

  /**
   * Checks if the current user can access the specified task's data.
   *
   * @param taskId The ID of the task to check access for.
   * @return true if the current user can access
   *     the specified task's data, false otherwise.
   */
  public boolean canAccessTask(Long taskId) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    boolean isTaskOwner = userService.isUserTaskOwner(user.getId(), taskId);
    boolean isTaskAssignedToUser = userService.isUserAssignedToTask(user.getId(), taskId);
    log.info(
        "Checking task access for task id: {} - isAdmin: {}, isTaskOwner: {}, isTaskAssignedToUser: {}",
        taskId, isAdmin, isTaskOwner, isTaskAssignedToUser
    );
    return isAdmin || isTaskOwner || isTaskAssignedToUser;
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
    TeamDto team = teamService.getTeamByName(teamName);
    Role userRole = teamUserService.getRoleByTeamNameAndUsername(team.name(), jwt.getUsername());

    boolean hasPermission = ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS.stream()
        .anyMatch(roleName -> roleName.name().equals(userRole.getName()));
    boolean isUserInTeam = teamUserService.existsByUserIdAndTeamId(jwt.getId(), team.id());
    boolean canAccess = (jwt.getId().equals(user.getId()) || hasPermission) && isUserInTeam;

    log.info(
        "Access check for report - Team: {}, User has permission: {}, In team: {}, Access granted: {}",
        teamName, hasPermission, isUserInTeam, canAccess
    );

    return canAccess;
  }







  /**
   * Checks if the current user has the necessary permissions to create a project.
   *
   * @return true if the user has the necessary permissions to create a project, false otherwise.
   */
  public boolean canCreateProject() {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean hasPermission = hasAnyRole(ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS);
    log.info(
        "Checking project creation permission for user with username: {} - hasPermission: {}",
        user.getUsername(), hasPermission
    );
    return hasPermission;
  }

  /**
   * Checks if the current user has the necessary permissions to create a team.
   *
   * @return true if the user has the necessary permissions to create a team, false otherwise.
   */
  public boolean canCreateTeam() {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean hasPermission = hasAnyRole(ALLOWED_ROLES_FOR_TEAM_PROJECT_AND_REPORT_ACCESS);
    log.info(
        "Checking team creation permission for user with username: {} - hasPermission: {}",
        user.getUsername(), hasPermission
    );
    return hasPermission;
  }

  private boolean hasAnyRole(List<RoleName> roles) {
    Authentication authentication = getContext().getAuthentication();
    boolean hasRole = roles.stream()
        .map(RoleName::name)
        .map(SimpleGrantedAuthority::new)
        .anyMatch(authentication.getAuthorities()::contains);
    log.info("Checking roles: {} - hasRole: {}", roles, hasRole);
    return hasRole;
  }
}
