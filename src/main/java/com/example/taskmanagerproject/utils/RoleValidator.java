package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_DESCRIPTION_TOO_LONG;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NAME_INVALID_FORMAT;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NAME_LENGTH_INVALID;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NAME_NULL_OR_EMPTY;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates the properties of a Role and RoleDto.
 *
 * <p>This class contains methods to validate role data, such as ensuring the role exists,
 * checking the length and format of role names, and verifying the validity of the role description.
 */
@Component
@RequiredArgsConstructor
public class RoleValidator {

  private final RoleRepository roleRepository;

  /**
   * Validates the given RoleDto object.
   *
   * @param roleDto The RoleDto to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateRoleDto(final RoleDto roleDto) {
    validateRoleData(roleDto);
    validateRoleExists(roleDto);
    validateRoleNameLength(roleDto);
    validateRoleDescriptionLength(roleDto);
    validateRoleNameFormat(roleDto);
  }

  /**
   * Validates the given RoleDto object against the existing Role entity.
   *
   * @param role    The existing Role entity.
   * @param roleDto The RoleDto to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateRoleDto(final Role role, final RoleDto roleDto) {
    validateRoleData(roleDto);
    validateRoleExists(role, roleDto);
    validateRoleNameLength(roleDto);
    validateRoleDescriptionLength(roleDto);
    validateRoleNameFormat(roleDto);
  }

  /**
   * Validates if the role already exists in the system.
   *
   * @param roleDto The RoleDto to check.
   * @throws ValidationException If the role already exists.
   */
  private void validateRoleExists(RoleDto roleDto) {
    if (roleRepository.existsByName(roleDto.getName().toUpperCase())) {
      throw new ValidationException(ROLE_ALREADY_EXISTS);
    }
  }

  /**
   * Validates if the role already exists in the system.
   *
   * @param role    The existing Role entity.
   * @param roleDto The RoleDto to check.
   * @throws ValidationException If the role already exists.
   */
  private void validateRoleExists(Role role, RoleDto roleDto) {
    if (!role.getName().equals(roleDto.getName().toUpperCase())
        && roleRepository.existsByName(roleDto.getName().toUpperCase())) {
      throw new ValidationException(ROLE_ALREADY_EXISTS);
    }
  }

  /**
   * Validates the basic role data such as name and description.
   *
   * @param roleDto The role data transfer object to validate.
   * @throws ValidationException If the role data is invalid.
   */
  private void validateRoleData(RoleDto roleDto) {
    if (roleDto.getName() == null || roleDto.getName().trim().isEmpty()) {
      throw new ValidationException(ROLE_NAME_NULL_OR_EMPTY);
    }
  }

  /**
   * Validates the length of the role name.
   *
   * @param roleDto The role data transfer object to validate.
   * @throws ValidationException If the role name is too short or too long.
   */
  private void validateRoleNameLength(RoleDto roleDto) {
    String roleName = roleDto.getName().trim();
    if (roleName.length() < 3 || roleName.length() > 50) {
      throw new ValidationException(ROLE_NAME_LENGTH_INVALID);
    }
  }

  /**
   * Validates the length of the role description.
   *
   * @param roleDto The role data transfer object to validate.
   * @throws ValidationException If the role description is too long or too short.
   */
  private void validateRoleDescriptionLength(RoleDto roleDto) {
    String description = roleDto.getDescription().trim();
    if (description.length() > 200) {
      throw new ValidationException(ROLE_DESCRIPTION_TOO_LONG);
    }
  }

  /**
   * Validates that the role name has a valid format (alphanumeric and underscores).
   *
   * @param roleDto The role data transfer object to validate.
   * @throws ValidationException If the role name contains invalid characters.
   */
  private void validateRoleNameFormat(RoleDto roleDto) {
    String roleName = roleDto.getName().trim();
    if (!roleName.matches("^[a-zA-Z0-9_]+$")) {
      throw new ValidationException(ROLE_NAME_INVALID_FORMAT);
    }
  }
}
