package com.example.taskmanagerproject.utils.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.entities.roles.Role;
import org.junit.jupiter.api.Test;

class RoleFactoryTest {

  @Test
  void createRoleFromRequest_shouldCreateRole() {
    RoleDto roleDto = new RoleDto(100L, "Member", "Team member role");

    Role result = RoleFactory.createRoleFromRequest(roleDto);

    assertEquals("MEMBER", result.getName());
    assertEquals("Team member role", result.getDescription());
  }

  @Test
  void createRoleFromRequest_shouldHandleEmptyDescription() {
    RoleDto roleDto = new RoleDto(101L, "Admin123", null);

    Role result = RoleFactory.createRoleFromRequest(roleDto);

    assertEquals("ADMIN123", result.getName());
    assertNull(result.getDescription());
  }

  @Test
  void createRoleFromRequest_shouldHandleAllLowerCase() {
    RoleDto roleDto = new RoleDto(103L, "developer", "Builds features");

    Role result = RoleFactory.createRoleFromRequest(roleDto);

    assertEquals("DEVELOPER", result.getName());
    assertEquals("Builds features", result.getDescription());
  }

  @Test
  void createRoleFromRequest_shouldHandleEmptyName() {
    RoleDto roleDto = new RoleDto(106L, "", "Empty name");

    Role result = RoleFactory.createRoleFromRequest(roleDto);

    assertEquals("", result.getName());
    assertEquals("Empty name", result.getDescription());
  }
}
