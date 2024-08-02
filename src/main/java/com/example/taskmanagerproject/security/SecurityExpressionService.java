package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.RoleName.ADMIN;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.RoleName;
import com.example.taskmanagerproject.services.UserService;
import java.util.Arrays;
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

  /**
   * Checks if the current user has the ADMIN role.
   *
   * @return true if the current user has the 'ADMIN' role, false otherwise.
   */
  public boolean hasRoleAdmin() {
    boolean isAdmin = hasAnyRole(ADMIN);
    log.info("Checking if user has ADMIN role - isAdmin: {}", isAdmin);
    return isAdmin;
  }

  /**
   * Checks if the current user can access the specified user's data by slug.
   *
   * @param slug The slug of the user to check.
   * @return true if the current user is an admin or matches the user's slug.
   */
  public boolean hasRoleAdmin(final String slug) {
    boolean isAdmin = hasAnyRole(ADMIN);
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
  public boolean hasRoleAdmin(Long id) {
    boolean isAdmin = hasAnyRole(ADMIN);
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    log.info("Checking access for user ID: {} - isAdmin: {}", id, isAdmin);
    return user.getId().equals(id) || isAdmin;
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

  private boolean hasAnyRole(final RoleName... roles) {
    Authentication authentication = getContext().getAuthentication();
    boolean hasRole = Arrays.stream(roles)
        .map(RoleName::name)
        .map(SimpleGrantedAuthority::new)
        .anyMatch(authentication.getAuthorities()::contains);
    log.info("Checking roles: {} - hasRole: {}", Arrays.toString(roles), hasRole);
    return hasRole;
  }
}
