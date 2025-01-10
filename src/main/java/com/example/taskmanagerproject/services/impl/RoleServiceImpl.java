package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_HIERARCHY_NOT_FOUND;
import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.users.RoleDto;
import com.example.taskmanagerproject.dtos.users.RoleHierarchyDto;
import com.example.taskmanagerproject.dtos.users.RoleHierarchyListDto;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.RoleHierarchy;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.RoleHierarchyRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.services.RoleService;
import com.example.taskmanagerproject.utils.factories.RoleFactory;
import com.example.taskmanagerproject.utils.factories.RoleHierarchyFactory;
import com.example.taskmanagerproject.utils.mappers.RoleHierarchyMapper;
import com.example.taskmanagerproject.utils.mappers.RoleMapper;
import com.example.taskmanagerproject.utils.validators.RoleValidator;
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
 * Implementation of the RoleService interface.
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
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));
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
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

    roleValidator.validateRoleDto(roleDto, existingRole);

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
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

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
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + roleName));

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
      RoleHierarchy roleHierarchy = findByHigherRoleNameAndLowerRoleName(roleHierarchyDto.higherRole().name(), roleHierarchyDto.lowerRole().name())
          .orElseThrow(() -> new ResourceNotFoundException(
            format(ROLE_HIERARCHY_NOT_FOUND, roleHierarchyDto.higherRole().name(), roleHierarchyDto.lowerRole().name())
          ));
      roleHierarchyRepository.delete(roleHierarchy);
    }
  }

  private Optional<RoleHierarchy> findByHigherRoleNameAndLowerRoleName(String higherRoleName, String lowerRoleName) {
    return roleHierarchyRepository.findAll().stream()
      .filter(rh -> rh.getHigherRole().getName().equals(higherRoleName) && rh.getLowerRole().getName().equals(lowerRoleName))
      .findFirst();
  }
}
