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

  @Override
  @Cacheable(value = "roles", key = "'all_roles'")
  public List<RoleDto> getAllRoles() {
    return roleRepository.findAll().stream().map(roleMapper::toDto).toList();
  }

  @Override
  @Cacheable(value = "roles", key = "#roleName")
  public RoleDto getRoleByName(String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));
    return roleMapper.toDto(role);
  }

  @Override
  @Transactional
  @CachePut(value = "roles", key = "#roleDto.name")
  public RoleDto createRole(RoleDto roleDto) {
    roleValidator.validateRoleDto(roleDto);
    Role newRole = RoleFactory.createRoleFromRequest(roleDto);
    roleRepository.save(newRole);
    return roleMapper.toDto(newRole);
  }

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

  @Override
  @Transactional
  @CacheEvict(value = "roles", key = "#roleName")
  public void deleteRole(String roleName) {
    Role existingRole = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    roleRepository.delete(existingRole);
  }

  @Override
  @Transactional
  public List<RoleHierarchyDto> createRoleHierarchies(List<RoleHierarchyDto> roleHierarchyDtoList) {
    return roleHierarchyDtoList.stream()
      .map(roleHierarchyFactory::createRoleHierarchyFromDto)
      .map(roleHierarchyRepository::save)
      .map(roleHierarchyMapper::toDto)
      .toList();
  }

  @Override
  public RoleHierarchyListDto findRoleWithHierarchy(String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    List<RoleDto> higherRoles = roleHierarchyRepository.findByLowerRole(role).stream()
        .map(roleHierarchy -> roleMapper.toDto(roleHierarchy.getHigherRole()))
        .collect(Collectors.toList());

    List<RoleDto> lowerRoles = roleHierarchyRepository.findByHigherRole(role).stream()
        .map(roleHierarchy -> roleMapper.toDto(roleHierarchy.getLowerRole()))
        .collect(Collectors.toList());

    return new RoleHierarchyListDto(role.getName(), higherRoles, lowerRoles);
  }

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
