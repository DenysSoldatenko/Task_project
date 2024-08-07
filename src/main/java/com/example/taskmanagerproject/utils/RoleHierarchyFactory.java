package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND;

import com.example.taskmanagerproject.dtos.RoleHierarchyDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleHierarchy;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating RoleHierarchy instances.
 */
@Component
@RequiredArgsConstructor
public final class RoleHierarchyFactory {

  private final RoleRepository roleRepository;

  /**
   * Creates a new RoleHierarchy entity from a RoleHierarchyDto.
   *
   * @param roleHierarchyDto The RoleHierarchyDto containing higher and lower roles.
   * @return A new RoleHierarchy entity.
   */
  public RoleHierarchy createRoleHierarchyFromDto(final RoleHierarchyDto roleHierarchyDto) {
    Role higherRole = getRoleFromDto(roleHierarchyDto.higherRole().name());
    Role lowerRole = getRoleFromDto(roleHierarchyDto.lowerRole().name());

    return buildRoleHierarchy(higherRole, lowerRole);
  }

  /**
   * Retrieves a Role entity from the database based on the role name.
   *
   * @param roleName The name of the role to retrieve.
   * @return The Role entity.
   */
  private Role getRoleFromDto(final String roleName) {
    return roleRepository.findByName(roleName)
      .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND));
  }

  /**
   * Builds a RoleHierarchy entity from the higher and lower roles.
   *
   * @param higherRole The higher role in the hierarchy.
   * @param lowerRole  The lower role in the hierarchy.
   * @return A new RoleHierarchy entity.
   */
  private RoleHierarchy buildRoleHierarchy(final Role higherRole, final Role lowerRole) {
    RoleHierarchy roleHierarchy = new RoleHierarchy();
    roleHierarchy.setHigherRole(higherRole);
    roleHierarchy.setLowerRole(lowerRole);
    return roleHierarchy;
  }
}
