package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.dtos.roles.RoleHierarchyDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.roles.RoleHierarchy;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleHierarchyFactoryTest {

  private RoleRepository roleRepository;
  private RoleHierarchyFactory factory;

  @BeforeEach
  void setUp() {
    roleRepository = mock(RoleRepository.class);
    factory = new RoleHierarchyFactory(roleRepository);
  }

  @Test
  void createRoleHierarchyFromDto_shouldReturnRoleHierarchy_givenValidRoles() {
    Role higher = new Role();
    higher.setId(1L);
    higher.setName("MANAGER");

    Role lower = new Role();
    lower.setId(2L);
    lower.setName("DEVELOPER");

    RoleDto higherDto = new RoleDto(100L, "MANAGER", "Description");
    RoleDto lowerDto = new RoleDto(100L, "DEVELOPER", "Description");

    RoleHierarchyDto dto = new RoleHierarchyDto(higherDto, lowerDto);

    when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(higher));
    when(roleRepository.findByName("DEVELOPER")).thenReturn(Optional.of(lower));

    RoleHierarchy result = factory.createRoleHierarchyFromDto(dto);

    assertNotNull(result);
    assertEquals("MANAGER", result.getHigherRole().getName());
    assertEquals("DEVELOPER", result.getLowerRole().getName());
  }

  @Test
  void createRoleHierarchyFromDto_shouldThrow_whenHigherRoleNotFound() {
    RoleDto higherDto = new RoleDto(105L, "UNKNOWN_ROLE", "Description");
    RoleDto lowerDto = new RoleDto(106L, "DEVELOPER", "Description");

    RoleHierarchyDto dto = new RoleHierarchyDto(higherDto, lowerDto);

    when(roleRepository.findByName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> factory.createRoleHierarchyFromDto(dto));

    assertTrue(ex.getMessage().contains(ROLE_NOT_FOUND_WITH_NAME + "UNKNOWN_ROLE"));
  }

  @Test
  void createRoleHierarchyFromDto_shouldThrow_whenLowerRoleNotFound() {
    Role higher = new Role();
    higher.setId(1L);
    higher.setName("MANAGER");

    RoleDto higherDto = new RoleDto(110L, "MANAGER", "Description");
    RoleDto lowerDto = new RoleDto(111L, "NON_EXISTENT", "Description");

    RoleHierarchyDto dto = new RoleHierarchyDto(higherDto, lowerDto);

    when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(higher));
    when(roleRepository.findByName("NON_EXISTENT")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> factory.createRoleHierarchyFromDto(dto));

    assertTrue(ex.getMessage().contains(ROLE_NOT_FOUND_WITH_NAME + "NON_EXISTENT"));
  }
}
