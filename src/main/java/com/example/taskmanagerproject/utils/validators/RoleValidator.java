package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_ALREADY_EXISTS;
import static java.util.Arrays.stream;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating role data.
 */
@Component
public class RoleValidator extends BaseValidator<RoleDto> {

  private final RoleRepository roleRepository;

  /**
   * Constructor for creating a RoleValidator instance.
   *
   * @param validator The validator object used to validate the role.
   * @param userRepository The repository responsible for accessing user data.
   * @param roleRepository The repository responsible for accessing role data.
   */
  public RoleValidator(Validator validator, UserRepository userRepository, RoleRepository roleRepository) {
    super(validator, userRepository);
    this.roleRepository = roleRepository;
  }

  /**
   * Validates a RoleDto object.
   *
   * @param roleDto The RoleDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateRoleDto(RoleDto roleDto, Role... existingRole) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(roleDto, errorMessages);
    validateRoleNameUniqueness(roleDto, errorMessages, existingRole);
    throwIfErrorsExist(errorMessages);
  }

  private void validateRoleNameUniqueness(RoleDto roleDto, Set<String> errorMessages, Role... existingRoles) {
    String dtoName = roleDto.name();
    String existingName = stream(existingRoles).findFirst().map(Role::getName).orElse(null);
    if (!dtoName.equals(existingName) && roleRepository.existsByName(dtoName)) {
      errorMessages.add(ROLE_ALREADY_EXISTS + roleDto.name());
    }
  }
}
