package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_HIERARCHY_NOT_FOUND;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND_WITH_NAME;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.dtos.RoleHierarchyDto;
import com.example.taskmanagerproject.dtos.RoleHierarchyListDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleHierarchy;
import com.example.taskmanagerproject.exceptions.RoleHierarchyNotFoundException;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.mappers.RoleHierarchyMapper;
import com.example.taskmanagerproject.mappers.RoleMapper;
import com.example.taskmanagerproject.repositories.RoleHierarchyRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.services.RoleService;
import com.example.taskmanagerproject.utils.RoleFactory;
import com.example.taskmanagerproject.utils.RoleHierarchyFactory;
import com.example.taskmanagerproject.utils.RoleValidator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  private final RoleMapper roleMapper;
  private final RoleValidator roleValidator;
  private final RoleRepository roleRepository;

  private final RoleHierarchyMapper roleHierarchyMapper;
  private final RoleHierarchyFactory roleHierarchyFactory;
  private final RoleHierarchyRepository roleHierarchyRepository;

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
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));
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
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    roleValidator.validateRoleDto(existingRole, roleDto);

    existingRole.setName(roleDto.name());
    existingRole.setDescription(roleDto.description());

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
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    roleRepository.delete(existingRole);
  }

  /**
   * Creates role hierarchies from a list of RoleHierarchyDto objects.
   * Saves each hierarchy into the repository and returns
   * the created hierarchy list as RoleHierarchyDto.
   *
   * @param roleHierarchyDtoList A list of RoleHierarchyDto objects to create.
   * @return A list of RoleHierarchyDto objects that were created and saved.
   */
  @Override
  @Transactional
  public List<RoleHierarchyDto> createRoleHierarchies(List<RoleHierarchyDto> roleHierarchyDtoList) {
    return roleHierarchyDtoList.stream()
      .map(roleHierarchyFactory::createRoleHierarchyFromDto)
      .map(roleHierarchyRepository::save)
      .map(roleHierarchyMapper::toDto)
      .toList();
  }

  /**
   * Finds a role and its higher and lower roles in the hierarchy.
   * This method retrieves the given role by name, then finds its higher and lower roles
   * in the hierarchy, returning them as a RoleHierarchyListDto.
   *
   * @param roleName The name of the role to retrieve (e.g., ADMIN).
   * @return A RoleHierarchyListDto containing the role and its higher and lower roles.
   * @throws RoleNotFoundException If the role with the specified name does not exist.
   */
  @Override
  public RoleHierarchyListDto findRoleWithHierarchy(String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    List<RoleHierarchy> higherRoleHierarchies = roleHierarchyRepository.findByLowerRole(role);
    List<RoleDto> higherRoles = higherRoleHierarchies.stream()
        .map(roleHierarchy -> roleMapper.toDto(roleHierarchy.getHigherRole()))
        .collect(Collectors.toList());

    List<RoleHierarchy> lowerRoleHierarchies = roleHierarchyRepository.findByHigherRole(role);
    List<RoleDto> lowerRoles = lowerRoleHierarchies.stream()
        .map(roleHierarchy -> roleMapper.toDto(roleHierarchy.getLowerRole()))
        .collect(Collectors.toList());

    return new RoleHierarchyListDto(role.getName(), higherRoles, lowerRoles);
  }

  /**
   * Deletes role hierarchies from a list of RoleHierarchyDto objects.
   * For each role hierarchy, the corresponding record is found and deleted from the repository.
   *
   * @param roleHierarchyDtoList A list of RoleHierarchyDto objects to delete.
   * @throws RoleHierarchyNotFoundException If any of the specified role hierarchies are not found.
   */
  @Override
  @Transactional
  public void deleteRoleHierarchies(List<RoleHierarchyDto> roleHierarchyDtoList) {
    for (RoleHierarchyDto roleHierarchyDto : roleHierarchyDtoList) {
      RoleHierarchy roleHierarchy = findByHigherRoleNameAndLowerRoleName(
          roleHierarchyDto.higherRole().name(),
          roleHierarchyDto.lowerRole().name()
      ).orElseThrow(() -> new RoleHierarchyNotFoundException(
          format(
            ROLE_HIERARCHY_NOT_FOUND,
            roleHierarchyDto.higherRole().name(),
            roleHierarchyDto.lowerRole().name()
          )
      ));

      roleHierarchyRepository.delete(roleHierarchy);
    }
  }

  /**
   * Finds a role hierarchy by the names of the higher and lower roles.
   *
   * @param higherRoleName The name of the higher role in the hierarchy.
   * @param lowerRoleName The name of the lower role in the hierarchy.
   * @return An Optional containing the RoleHierarchy if found, otherwise empty.
   */
  private Optional<RoleHierarchy> findByHigherRoleNameAndLowerRoleName(
      String higherRoleName, String lowerRoleName
  ) {
    return roleHierarchyRepository.findAll().stream()
      .filter(
        rh -> rh.getHigherRole().getName().equals(higherRoleName)
          && rh.getLowerRole().getName().equals(lowerRoleName)
      )
      .findFirst();
  }
}
