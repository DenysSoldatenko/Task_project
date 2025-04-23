package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_ALREADY_EXISTS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.roles.RoleDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleValidatorTest {

  private Validator validator;
  private RoleRepository roleRepository;
  private RoleValidator roleValidator;

  private final RoleDto validRoleDto = new RoleDto(100L, "MEMBER", "Some description");

  @BeforeEach
  void setUp() {
    validator = mock(Validator.class);
    roleRepository = mock(RoleRepository.class);
    roleValidator = new RoleValidator(validator, roleRepository);
  }

  @Test
  void validateRoleDto_shouldPassWithValidInput() {
    when(validator.validate(validRoleDto)).thenReturn(Collections.emptySet());
    when(roleRepository.existsByName("MEMBER")).thenReturn(false);

    assertDoesNotThrow(() -> roleValidator.validateRoleDto(validRoleDto));
  }

  @Test
  void validateRoleDto_shouldPassWhenUpdatingSameRole() {
    Role existingRole = new Role();
    existingRole.setName("MEMBER");

    when(validator.validate(validRoleDto)).thenReturn(Collections.emptySet());
    when(roleRepository.existsByName("MEMBER")).thenReturn(true);

    assertDoesNotThrow(() -> roleValidator.validateRoleDto(validRoleDto, existingRole));
  }

  @Test
  void validateRoleDto_shouldThrowIfRoleNameExistsAndNotUpdating() {
    when(validator.validate(validRoleDto)).thenReturn(Collections.emptySet());
    when(roleRepository.existsByName("MEMBER")).thenReturn(true);

    ValidationException ex = assertThrows(ValidationException.class, () -> roleValidator.validateRoleDto(validRoleDto));
    assertTrue(ex.getMessage().contains(ROLE_ALREADY_EXISTS + "MEMBER"));
  }

  @Test
  void validateRoleDto_shouldThrowIfRoleNameExistsWhenUpdatingDifferentRole() {
    Role existingRole = new Role();
    existingRole.setName("ADMIN");

    when(validator.validate(validRoleDto)).thenReturn(Collections.emptySet());
    when(roleRepository.existsByName("MEMBER")).thenReturn(true);

    ValidationException ex = assertThrows(ValidationException.class, () -> roleValidator.validateRoleDto(validRoleDto, existingRole));
    assertTrue(ex.getMessage().contains(ROLE_ALREADY_EXISTS + "MEMBER"));
  }

  @Test
  void validateRoleDto_shouldThrowIfConstraintViolationsExist() {
    ConstraintViolation<RoleDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Name must not be blank");

    when(validator.validate(validRoleDto)).thenReturn(Set.of(violation));

    ValidationException ex = assertThrows(ValidationException.class, () -> roleValidator.validateRoleDto(validRoleDto));
    assertTrue(ex.getMessage().contains("Name must not be blank"));
  }
}