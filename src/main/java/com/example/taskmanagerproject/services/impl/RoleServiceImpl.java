package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.mappers.RoleMapper;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.services.RoleService;
import com.example.taskmanagerproject.utils.RoleFactory;
import com.example.taskmanagerproject.utils.RoleValidator;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for handling business logic related to roles in the application.
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;
  private final RoleValidator roleValidator;
  private final RoleMapper roleMapper;

  /**
   * Get all roles.
   *
   * @return A list of all roles in the system.
   */
  @Override
  @Cacheable(value = "roles", key = "'all_roles'")
  public List<RoleDto> getAllRoles() {
    return roleRepository.findAll().stream().map(roleMapper::toDto).toList();
  }

  /**
   * Get a specific role by its name.
   *
   * @param roleName The name of the role (e.g., ADMIN, USER).
   * @return The role with the specified name.
   */
  @Override
  @Cacheable(value = "roles", key = "#roleName")
  public RoleDto getRoleByName(String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND));
    return roleMapper.toDto(role);
  }

  /**
   * Create a new role.
   *
   * @param roleDto Data Transfer Object containing the role information.
   * @return The created role.
   */
  @Override
  @Transactional
  @CachePut(value = "roles", key = "#roleDto.name")
  public RoleDto createRole(RoleDto roleDto) {
    roleValidator.validateRoleDto(roleDto);
    Role newRole = RoleFactory.createRoleFromRequest(roleDto);
    roleRepository.save(newRole);
    return roleMapper.toDto(newRole);
  }

  /**
   * Update an existing role.
   *
   * @param roleDto  Data Transfer Object containing the updated role information.
   * @return The updated role.
   */
  @Override
  @Transactional
  @CachePut(value = "roles", key = "#roleDto.name")
  public RoleDto updateRole(String roleName, RoleDto roleDto) {
    Role existingRole = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND));

    roleValidator.validateRoleDto(existingRole, roleDto);

    existingRole.setName(roleDto.getName());
    existingRole.setDescription(roleDto.getDescription());

    roleRepository.save(existingRole);
    return roleMapper.toDto(existingRole);
  }

  /**
   * Delete a role by its name.
   *
   * @param roleName The name of the role to delete.
   */
  @Override
  @Transactional
  @CacheEvict(value = "roles", key = "#roleName")
  public void deleteRole(String roleName) {
    Role existingRole = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND));

    roleRepository.delete(existingRole);
  }
}
