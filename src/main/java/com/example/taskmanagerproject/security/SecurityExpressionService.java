package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.services.UserService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for security expressions related to user and task access.
 */
@Service("expressionService")
@RequiredArgsConstructor
public class SecurityExpressionService {

  private static final Logger logger = LoggerFactory.getLogger(SecurityExpressionService.class);
  private final UserService userService;

  /**
   * Checks if the current user can access the specified user's data.
   *
   * @param id The ID of the user to check access for.
   * @return true if the current user can access the specified user's data, false otherwise.
   */
  public boolean canAccessUser(Long id) {
    JwtEntity user = (JwtEntity) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    boolean isAdmin = hasAnyRole(Role.ROLE_ADMIN);
    logger.info("Checking user access for user id: {} - isAdmin: {}", id, isAdmin);
    return user.getId().equals(id) || isAdmin;
  }

  /**
   * Checks if the current user can access the specified task's data.
   *
   * @param taskId The ID of the task to check access for.
   * @return true if the current user can access the specified task's data, false otherwise.
   */
  public boolean canAccessTask(Long taskId) {
    JwtEntity user = (JwtEntity) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    boolean isTaskOwner = userService.isUserTaskOwner(user.getId(), taskId);
    logger.info("Checking task access for task id: {} - isTaskOwner: {}", taskId, isTaskOwner);
    return isTaskOwner;
  }

  private boolean hasAnyRole(Role... roles) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean hasRole = Arrays.stream(roles)
        .map(Role::name)
        .map(SimpleGrantedAuthority::new)
        .anyMatch(authentication.getAuthorities()::contains);
    logger.info("Checking roles: {} - hasRole: {}", Arrays.toString(roles), hasRole);
    return hasRole;
  }
}
