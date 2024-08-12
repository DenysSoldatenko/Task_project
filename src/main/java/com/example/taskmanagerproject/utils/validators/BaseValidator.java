package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.entities.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.RoleName.TEAM_LEAD;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.util.Arrays.asList;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * BaseValidator provides common validation methods for entities and DTOs.
 * It includes utilities for validating constraints,
 * handling errors, and checking roles or entity names.
 *
 * @param <T> The type of object to be validated (e.g., UserDto, TeamDto).
 */
@Component
@AllArgsConstructor
public abstract class BaseValidator<T> {

  private static final List<String> CREATION_ALLOWED_ROLES = asList(
      ADMIN.name(), PRODUCT_OWNER.name(), SCRUM_MASTER.name(), MANAGER.name(), TEAM_LEAD.name()
  );

  private final Validator validator;
  private final UserRepository userRepository;

  /**
   * Validates the constraints of the DTO object.
   *
   * @param dto           The object to validate.
   * @param errorMessages The collection to hold error messages.
   */
  protected void validateConstraints(T dto, Set<String> errorMessages) {
    Set<ConstraintViolation<T>> violations = validator.validate(dto);
    for (ConstraintViolation<T> violation : violations) {
      errorMessages.add(violation.getMessage());
    }
  }

  /**
   * Throws ValidationException if there are any errors.
   *
   * @param errorMessages The collection of error messages.
   */
  protected void throwIfErrorsExist(Set<String> errorMessages) {
    if (!errorMessages.isEmpty()) {
      throw new ValidationException(String.join(", ", errorMessages));
    }
  }

  /**
   * Validates if the creator role is valid by checking if the role of the user is allowed
   * to create a resource. If the role is not valid, adds an error message.
   *
   * @param dto The UserDto object containing the creator's username.
   * @param errorMessage The error message to be added if validation fails.
   * @param errorMessages The set where the error message will be added if the role is invalid.
   * @throws ValidationException if the creator's role is not allowed.
   */
  protected void validateCreatorRole(
      final UserDto dto,
      final String errorMessage,
      final Set<String> errorMessages
  ) {
    User creator = userRepository.findByUsername(dto.username())
        .orElseThrow(() -> new ValidationException(USER_NOT_FOUND_WITH_USERNAME + dto.username()));

    if (!CREATION_ALLOWED_ROLES.contains(creator.getRole().getName())) {
      errorMessages.add(errorMessage + dto.username());
    }
  }

  /**
   * Validates whether the name provided in the DTO is already taken. If the name is not the same
   * as the existing name and the name already exists in the repository, an error message is added.
   *
   * @param dtoName The name in the DTO to validate.
   * @param existingName The name of the existing entity in the repository.
   * @param existsByName A boolean flag indicating
   *                     whether the name already exists in the repository.
   * @param errorMessages The collection to hold error messages if validation fails.
   * @param errorMessagePrefix The prefix to be added to the error message if validation fails.
   */
  protected void validateNameTaken(
      final String dtoName,
      final String existingName,
      final boolean existsByName,
      final Set<String> errorMessages,
      final String errorMessagePrefix
  ) {
    if (!dtoName.equals(existingName) && existsByName) {
      errorMessages.add(errorMessagePrefix + dtoName);
    }
  }
}
