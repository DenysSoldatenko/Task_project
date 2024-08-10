package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.RoleName.TEAM_LEAD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.RoleName;
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
@Service("expressionService")
@RequiredArgsConstructor
public class SecurityExpressionService {

  private final UserService userService;

  private static final List<RoleName> PROJECT_CREATION_ALLOWED_ROLES = asList(
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
  public boolean canAccessUserDataBySlug(final String slug) {
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
  public boolean canAccessUserDataById(final Long id) {
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
  public boolean canAccessProject(final String projectName) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isAdmin = hasAnyRole(singletonList(ADMIN));
    boolean isProjectCreator = userService.isProjectCreator(projectName, user.getId());
    log.info(
        "Checking access for user ID: {} on project name: {} - isAdmin: {}, isProjectCreator: {}",
        user.getId(), projectName, isAdmin, isProjectCreator
    );
    return isAdmin || isProjectCreator;
  }

  /**
   * Checks if the current user has the necessary permissions to create a project.
   *
   * @return true if the user has the necessary permissions to create a project, false otherwise.
   */
  public boolean canCreateProject() {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean hasPermission = hasAnyRole(PROJECT_CREATION_ALLOWED_ROLES);
    log.info(
        "Checking project creation permission for user with username: {} - hasPermission: {}",
        user.getUsername(), hasPermission
    );
    return hasPermission;
  }

  /**
   * Checks if the current user can access the specified task's data.
   *
   * @param taskId The ID of the task to check access for.
   * @return true if the current user can access
   *     the specified task's data, false otherwise.
   */
  public boolean canAccessTask(final Long taskId) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isTaskOwner = userService.isUserTaskOwner(user.getId(), taskId);
    log.info("Checking task access for task id: {} - isTaskOwner: {}", taskId, isTaskOwner);
    return isTaskOwner;
  }

  private boolean hasAnyRole(final List<RoleName> roles) {
    Authentication authentication = getContext().getAuthentication();
    boolean hasRole = roles.stream()
        .map(RoleName::name)
        .map(SimpleGrantedAuthority::new)
        .anyMatch(authentication.getAuthorities()::contains);
    log.info("Checking roles: {} - hasRole: {}", roles, hasRole);
    return hasRole;
  }
}
