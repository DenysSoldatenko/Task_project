package com.example.taskmanagerproject.security;

import static com.example.taskmanagerproject.entities.Role.ROLE_ADMIN;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.example.taskmanagerproject.entities.Role;
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
   * Checks if the current user can access the specified user's data.
   *
   * @param id The ID of the user to check access for.
   * @return true if the current user can
   *     access the specified user's data, false otherwise.
   */
  public boolean canAccessUser(final Long id) {
    JwtEntity user = (JwtEntity) getContext().getAuthentication().getPrincipal();
    boolean isAdmin = hasAnyRole(ROLE_ADMIN);
    log.info("Checking user access for user id: {} - isAdmin: {}", id, isAdmin);
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

  private boolean hasAnyRole(final Role... roles) {
    Authentication authentication = getContext().getAuthentication();
    boolean hasRole = Arrays.stream(roles)
        .map(Role::name)
        .map(SimpleGrantedAuthority::new)
        .anyMatch(authentication.getAuthorities()::contains);
    log.info("Checking roles: {} - hasRole: {}", Arrays.toString(roles), hasRole);
    return hasRole;
  }
}
