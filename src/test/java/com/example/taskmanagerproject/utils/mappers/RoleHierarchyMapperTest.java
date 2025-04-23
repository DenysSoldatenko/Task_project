package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.dtos.roles.RoleHierarchyDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.roles.RoleHierarchy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class RoleHierarchyMapperTest {

  private Role higherRole;
  private Role lowerRole;

  private RoleHierarchy roleHierarchy;
  private RoleHierarchyDto roleHierarchyDto;

  private RoleDto higherRoleDto;
  private RoleDto lowerRoleDto;

  private RoleHierarchyMapper roleHierarchyMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    roleHierarchyMapper = new RoleHierarchyMapperImpl();

    higherRole = new Role();
    higherRole.setName("ADMIN");

    lowerRole = new Role();
    lowerRole.setName("MEMBER");

    roleHierarchy = new RoleHierarchy();
    roleHierarchy.setHigherRole(higherRole);
    roleHierarchy.setLowerRole(lowerRole);

    higherRoleDto = new RoleDto(100L, "ADMIN123", "Admin role");
    lowerRoleDto = new RoleDto(101L, "MEMBER", "Member role");
    roleHierarchyDto = new RoleHierarchyDto(higherRoleDto, lowerRoleDto);
  }

  @Test
  void shouldMapRoleHierarchyToDto() {
    RoleHierarchyDto result = roleHierarchyMapper.toDto(roleHierarchy);

    assertEquals(higherRole.getName(), result.higherRole().name());
    assertEquals(lowerRole.getName(), result.lowerRole().name());
  }

  @Test
  void shouldMapRoleHierarchyDtoToEntity() {
    RoleHierarchy result = roleHierarchyMapper.toEntity(roleHierarchyDto);

    assertEquals(higherRoleDto.name(), result.getHigherRole().getName());
    assertEquals(lowerRoleDto.name(), result.getLowerRole().getName());
  }

  @Test
  void shouldHandleNullRoleHierarchy() {
    RoleHierarchyDto result = roleHierarchyMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullRoleHierarchyDto() {
    RoleHierarchy result = roleHierarchyMapper.toEntity(null);
    assertNull(result);
  }
}