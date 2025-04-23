package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.dtos.roles.RoleHierarchyDto;
import com.example.taskmanagerproject.dtos.roles.RoleHierarchyListDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.roles.RoleHierarchy;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.RoleHierarchyRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.utils.factories.RoleFactory;
import com.example.taskmanagerproject.utils.factories.RoleHierarchyFactory;
import com.example.taskmanagerproject.utils.mappers.RoleHierarchyMapper;
import com.example.taskmanagerproject.utils.mappers.RoleMapper;
import com.example.taskmanagerproject.utils.validators.RoleValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class RoleServiceImplTest {

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private RoleHierarchyRepository roleHierarchyRepository;

  @Mock
  private RoleMapper roleMapper;

  @Mock
  private RoleHierarchyMapper roleHierarchyMapper;

  @Mock
  private RoleValidator roleValidator;

  @Mock
  private RoleHierarchyFactory roleHierarchyFactory;

  @InjectMocks
  private RoleServiceImpl roleService;

  private Role role;
  private RoleDto roleDto;
  private RoleHierarchy roleHierarchy;
  private RoleHierarchyDto roleHierarchyDto;
  private final String roleName = "TestRole";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    role = mock(Role.class);
    roleDto = mock(RoleDto.class);
    roleHierarchy = mock(RoleHierarchy.class);
    roleHierarchyDto = mock(RoleHierarchyDto.class);

    when(role.getName()).thenReturn(roleName);
    when(roleDto.name()).thenReturn(roleName);
    when(roleDto.description()).thenReturn("Test Description");
    when(roleMapper.toDto(role)).thenReturn(roleDto);
    when(roleHierarchyMapper.toDto(roleHierarchy)).thenReturn(roleHierarchyDto);
    when(roleHierarchyDto.higherRole()).thenReturn(roleDto);
    when(roleHierarchyDto.lowerRole()).thenReturn(roleDto);
  }

  @Test
  void getAllRoles_shouldReturnRoleDtosWhenRolesExist() {
    when(roleRepository.findAll()).thenReturn(List.of(role));
    List<RoleDto> result = roleService.getAllRoles();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(roleDto, result.get(0));
    verify(roleRepository).findAll();
    verify(roleMapper).toDto(role);
  }

  @Test
  void getAllRoles_shouldReturnEmptyListWhenNoRoles() {
    when(roleRepository.findAll()).thenReturn(Collections.emptyList());
    List<RoleDto> result = roleService.getAllRoles();
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(roleRepository).findAll();
  }

  @Test
  void getRoleByName_shouldReturnRoleDtoWhenRoleExists() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    RoleDto result = roleService.getRoleByName(roleName);
    assertNotNull(result);
    assertEquals(roleDto, result);
    verify(roleRepository).findByName(roleName);
    verify(roleMapper).toDto(role);
  }

  @Test
  void getRoleByName_shouldThrowResourceNotFoundExceptionWhenRoleNotFound() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleByName(roleName));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME + roleName, exception.getMessage());
    verify(roleRepository).findByName(roleName);
  }

  @Test
  void getRoleByName_shouldHandleEmptyRoleName() {
    when(roleRepository.findByName("")).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleByName(""));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME, exception.getMessage());
    verify(roleRepository).findByName("");
  }

  @Test
  void createRole_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    doThrow(new IllegalArgumentException("Invalid role name")).when(roleValidator).validateRoleDto(roleDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> roleService.createRole(roleDto));
    assertEquals("Invalid role name", exception.getMessage());
    verify(roleValidator).validateRoleDto(roleDto);
  }

  @Test
  void updateRole_shouldUpdateAndReturnRoleDto() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    doNothing().when(roleValidator).validateRoleDto(eq(roleDto), eq(role));
    when(roleRepository.save(role)).thenReturn(role);
    RoleDto result = roleService.updateRole(roleName, roleDto);
    assertNotNull(result);
    assertEquals(roleDto, result);
    verify(roleRepository).findByName(roleName);
    verify(roleValidator).validateRoleDto(roleDto, role);
    verify(role).setName(roleName);
    verify(role).setDescription("Test Description");
    verify(roleRepository).save(role);
    verify(roleMapper).toDto(role);
  }

  @Test
  void updateRole_shouldThrowResourceNotFoundExceptionWhenRoleNotFound() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.updateRole(roleName, roleDto));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME + roleName, exception.getMessage());
    verify(roleRepository).findByName(roleName);
  }

  @Test
  void updateRole_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    doThrow(new IllegalArgumentException("Invalid role name")).when(roleValidator).validateRoleDto(roleDto, role);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> roleService.updateRole(roleName, roleDto));
    assertEquals("Invalid role name", exception.getMessage());
    verify(roleRepository).findByName(roleName);
    verify(roleValidator).validateRoleDto(roleDto, role);
  }

  @Test
  void deleteRole_shouldDeleteRoleWhenExists() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    doNothing().when(roleRepository).delete(role);
    roleService.deleteRole(roleName);
    verify(roleRepository).findByName(roleName);
    verify(roleRepository).delete(role);
  }

  @Test
  void deleteRole_shouldThrowResourceNotFoundExceptionWhenRoleNotFound() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(roleName));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME + roleName, exception.getMessage());
    verify(roleRepository).findByName(roleName);
  }

  @Test
  void deleteRole_shouldHandleEmptyRoleName() {
    when(roleRepository.findByName("")).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(""));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME, exception.getMessage());
    verify(roleRepository).findByName("");
  }

  @Test
  void createRoleHierarchies_shouldCreateAndReturnHierarchyDtos() {
    when(roleHierarchyFactory.createRoleHierarchyFromDto(roleHierarchyDto)).thenReturn(roleHierarchy);
    when(roleHierarchyRepository.save(roleHierarchy)).thenReturn(roleHierarchy);
    List<RoleHierarchyDto> result = roleService.createRoleHierarchies(List.of(roleHierarchyDto));
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(roleHierarchyDto, result.get(0));
    verify(roleHierarchyFactory).createRoleHierarchyFromDto(roleHierarchyDto);
    verify(roleHierarchyRepository).save(roleHierarchy);
    verify(roleHierarchyMapper).toDto(roleHierarchy);
  }

  @Test
  void createRoleHierarchies_shouldReturnEmptyListWhenInputEmpty() {
    List<RoleHierarchyDto> result = roleService.createRoleHierarchies(Collections.emptyList());
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void findRoleWithHierarchy_shouldReturnHierarchyListDtoWhenRoleExists() {
    Role higherRole = mock(Role.class);
    Role lowerRole = mock(Role.class);
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    when(roleHierarchyRepository.findByLowerRole(role)).thenReturn(List.of(roleHierarchy));
    when(roleHierarchyRepository.findByHigherRole(role)).thenReturn(List.of(roleHierarchy));
    when(roleHierarchy.getHigherRole()).thenReturn(higherRole);
    when(roleHierarchy.getLowerRole()).thenReturn(lowerRole);
    RoleHierarchyListDto result = roleService.findRoleWithHierarchy(roleName);
    assertNotNull(result);
    assertEquals(1, result.higherRoles().size());
    assertEquals(1, result.lowerRoles().size());
    verify(roleRepository).findByName(roleName);
    verify(roleHierarchyRepository).findByLowerRole(role);
    verify(roleHierarchyRepository).findByHigherRole(role);
    verify(roleMapper).toDto(higherRole);
    verify(roleMapper).toDto(lowerRole);
  }

  @Test
  void findRoleWithHierarchy_shouldThrowResourceNotFoundExceptionWhenRoleNotFound() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.findRoleWithHierarchy(roleName));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME + roleName, exception.getMessage());
    verify(roleRepository).findByName(roleName);
  }

  @Test
  void findRoleWithHierarchy_shouldReturnEmptyListsWhenNoHierarchies() {
    when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
    when(roleHierarchyRepository.findByLowerRole(role)).thenReturn(Collections.emptyList());
    when(roleHierarchyRepository.findByHigherRole(role)).thenReturn(Collections.emptyList());
    RoleHierarchyListDto result = roleService.findRoleWithHierarchy(roleName);
    assertNotNull(result);
    assertEquals(roleName, result.name());
    assertTrue(result.higherRoles().isEmpty());
    assertTrue(result.lowerRoles().isEmpty());
    verify(roleRepository).findByName(roleName);
    verify(roleHierarchyRepository).findByLowerRole(role);
    verify(roleHierarchyRepository).findByHigherRole(role);
  }

  @Test
  void deleteRoleHierarchies_shouldHandleEmptyHierarchyList() {
    roleService.deleteRoleHierarchies(Collections.emptyList());
  }

  @Test
  void deleteRoleHierarchies_shouldDeleteMatchingHierarchies() {
    RoleHierarchyDto dto = mock(RoleHierarchyDto.class);
    RoleDto higherRoleDto = mock(RoleDto.class);
    RoleDto lowerRoleDto = mock(RoleDto.class);
    when(dto.higherRole()).thenReturn(higherRoleDto);
    when(dto.lowerRole()).thenReturn(lowerRoleDto);
    when(higherRoleDto.name()).thenReturn("Higher");
    when(lowerRoleDto.name()).thenReturn("Lower");

    RoleHierarchy storedHierarchy = mock(RoleHierarchy.class);
    Role higherRoleEntity = mock(Role.class);
    Role lowerRoleEntity = mock(Role.class);

    when(storedHierarchy.getHigherRole()).thenReturn(higherRoleEntity);
    when(storedHierarchy.getLowerRole()).thenReturn(lowerRoleEntity);
    when(higherRoleEntity.getName()).thenReturn("Higher");
    when(lowerRoleEntity.getName()).thenReturn("Lower");
    when(roleHierarchyRepository.findAll()).thenReturn(List.of(storedHierarchy));
    roleService.deleteRoleHierarchies(List.of(dto));
    verify(roleHierarchyRepository).findAll();
    verify(roleHierarchyRepository).delete(storedHierarchy);
  }

  @Test
  void deleteRoleHierarchies_shouldHandleEmptyListGracefully() {
    assertDoesNotThrow(() -> roleService.deleteRoleHierarchies(List.of()));
    verifyNoInteractions(roleHierarchyRepository);
  }

  @Test
  void createRole_shouldValidateCreateAndReturnDto() {
    RoleDto requestDto = mock(RoleDto.class);
    Role roleEntity = mock(Role.class);
    RoleDto responseDto = mock(RoleDto.class);

    doNothing().when(roleValidator).validateRoleDto(requestDto);
    try (MockedStatic<RoleFactory> factory = mockStatic(RoleFactory.class)) {
      factory.when(() -> RoleFactory.createRoleFromRequest(requestDto)).thenReturn(roleEntity);

      when(roleRepository.save(roleEntity)).thenReturn(roleEntity);
      when(roleMapper.toDto(roleEntity)).thenReturn(responseDto);

      RoleDto result = roleService.createRole(requestDto);
      assertNotNull(result);
      assertEquals(responseDto, result);

      verify(roleValidator).validateRoleDto(requestDto);
      verify(roleRepository).save(roleEntity);
      verify(roleMapper).toDto(roleEntity);
    }
  }

  @Test
  void createRole_shouldPropagateValidationException() {
    RoleDto requestDto = mock(RoleDto.class);
    doThrow(new IllegalArgumentException("Invalid role")).when(roleValidator).validateRoleDto(requestDto);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> roleService.createRole(requestDto));
    assertEquals("Invalid role", ex.getMessage());
    verify(roleValidator).validateRoleDto(requestDto);
    verifyNoMoreInteractions(roleRepository, roleMapper);
  }
}