package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.security.RoleDto;
import com.example.taskmanagerproject.dtos.security.RoleHierarchyDto;
import com.example.taskmanagerproject.dtos.security.RoleHierarchyListDto;
import java.util.List;

/**
 * Service interface for managing roles.
 *
 * <p>This interface defines the methods for creating, retrieving, updating, and
 * deleting roles in the system. It allows interaction with roles using
 * {@link RoleDto} objects and provides business logic for role management.
 */
public interface RoleService {

  /**
   * Retrieves a list of all roles.
   *
   * @return A list of {@link RoleDto} objects representing all roles in the system.
   */
  List<RoleDto> getAllRoles();

  /**
   * Retrieves a role by its name.
   *
   * @param roleName The name of the role to retrieve (e.g., ADMIN, USER).
   * @return A {@link RoleDto} representing the requested role.
   */
  RoleDto getRoleByName(String roleName);

  /**
   * Creates a new role.
   *
   * @param roleDto The {@link RoleDto} containing the data for the new role.
   * @return A {@link RoleDto} representing the created role.
   */
  RoleDto createRole(RoleDto roleDto);

  /**
   * Updates an existing role by its name.
   *
   * @param roleName The name of the role to update.
   * @param roleDto The {@link RoleDto} containing the updated role data.
   * @return A {@link RoleDto} representing the updated role.
   */
  RoleDto updateRole(String roleName, RoleDto roleDto);

  /**
   * Deletes a role by its name.
   *
   * @param roleName The name of the role to delete.
   */
  void deleteRole(String roleName);

  /**
   * Creates multiple role hierarchies from a list of RoleHierarchyDto objects.
   *
   * @param roleHierarchyDtoList A list of RoleHierarchyDto objects representing
   *                             the role hierarchies to be created.
   * @return A list of RoleHierarchyDto objects representing the created role hierarchies.
   */
  List<RoleHierarchyDto> createRoleHierarchies(List<RoleHierarchyDto> roleHierarchyDtoList);

  /**
   * Finds a role along with its higher and lower roles in the hierarchy
   * based on the provided role name.
   *
   * @param roleName The name of the role to retrieve (e.g., ADMIN).
   * @return A RoleHierarchyListDto containing the role and its associated higher and lower roles.
   */
  RoleHierarchyListDto findRoleWithHierarchy(String roleName);

  /**
   * Deletes multiple role hierarchies based on a list of RoleHierarchyDto objects.
   *
   * @param roleHierarchyDtoList A list of RoleHierarchyDto objects representing
   *                             the role hierarchies to be deleted.
   */
  void deleteRoleHierarchies(List<RoleHierarchyDto> roleHierarchyDtoList);
}
