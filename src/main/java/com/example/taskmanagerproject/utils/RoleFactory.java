package com.example.taskmanagerproject.utils;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import lombok.experimental.UtilityClass;

/**
 * Factory class for creating Role instances from role requests.
 */
@UtilityClass
public class RoleFactory {

  /**
   * Creates a new Role instance from a role request.
   *
   * @param request The role request containing role information.
   * @return A new Role instance.
   */
  public Role createRoleFromRequest(final RoleDto request) {
    Role role = new Role();
    role.setName(request.name().toUpperCase());
    role.setDescription(request.description());
    return role;
  }
}
