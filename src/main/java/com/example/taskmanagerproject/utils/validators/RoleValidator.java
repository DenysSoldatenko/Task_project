package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_ALREADY_EXISTS;
import static java.lang.String.join;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Utility class for validating role data.
 */
@Component
@Validated
@AllArgsConstructor
public class RoleValidator {

  private final Validator validator;
  private final RoleRepository roleRepository;

  /**
   * Validates a RoleDto object.
   *
   * @param roleDto The RoleDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateRoleDto(final RoleDto roleDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(roleDto, errorMessages);
    validateRoleExists(roleDto, errorMessages);

    if (!errorMessages.isEmpty()) {
      throw new ValidationException(join(", ", errorMessages));
    }
  }

  /**
   * Validates a RoleDto object against an existing Role entity.
   *
   * @param role    The existing Role entity.
   * @param roleDto The RoleDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateRoleDto(final Role role, final RoleDto roleDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(roleDto, errorMessages);
    validateRoleExists(role, roleDto, errorMessages);

    if (!errorMessages.isEmpty()) {
      throw new ValidationException(join(", ", errorMessages));
    }
  }

  /**
   * Validates constraints such as null or empty values.
   *
   * @param roleDto The RoleDto object to validate.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateConstraints(final RoleDto roleDto, final Set<String> errorMessages) {
    Set<ConstraintViolation<RoleDto>> violations = validator.validate(roleDto);
    for (ConstraintViolation<RoleDto> violation : violations) {
      errorMessages.add(violation.getMessage());
    }
  }

  /**
   * Validates if the role already exists in the system.
   *
   * @param roleDto The RoleDto object to check.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateRoleExists(final RoleDto roleDto, final Set<String> errorMessages) {
    if (roleRepository.existsByName(roleDto.name().toUpperCase())) {
      errorMessages.add(ROLE_ALREADY_EXISTS + roleDto.name().toUpperCase());
    }
  }

  /**
   * Validates if the role already exists in the system (used during update).
   *
   * @param role The existing Role entity.
   * @param roleDto The RoleDto object to check.
   * @param errorMessages The collection to hold error messages.
   */
  private void validateRoleExists(
      final Role role,
      final RoleDto roleDto,
      final Set<String> errorMessages
  ) {
    if (!role.getName().equals(roleDto.name().toUpperCase())
        && roleRepository.existsByName(roleDto.name().toUpperCase())) {
      errorMessages.add(ROLE_ALREADY_EXISTS + roleDto.name().toUpperCase());
    }
  }
}
