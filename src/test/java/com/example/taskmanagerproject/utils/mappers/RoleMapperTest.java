package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.taskmanagerproject.dtos.users.RoleDto;
import com.example.taskmanagerproject.entities.users.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class RoleMapperTest {

  private RoleMapper roleMapper;

  private Role role;
  private RoleDto roleDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    roleMapper = new RoleMapperImpl();

    role = new Role();
    role.setName("MEMBER");
    role.setDescription("Team member role");

    roleDto = new RoleDto(100L, "MEMBER", "Team member role");
  }

  @Test
  void shouldMapRoleToDto() {
    RoleDto result = roleMapper.toDto(role);

    assertEquals("MEMBER", result.name());
    assertEquals("Team member role", result.description());
  }

  @Test
  void shouldMapRoleDtoToEntity() {
    Role result = roleMapper.toEntity(roleDto);

    assertEquals("MEMBER", result.getName());
    assertEquals("Team member role", result.getDescription());
  }

  @Test
  void shouldHandleNullRole() {
    RoleDto result = roleMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullRoleDto() {
    Role result = roleMapper.toEntity(null);
    assertNull(result);
  }
}