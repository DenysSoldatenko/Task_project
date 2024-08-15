package com.example.taskmanagerproject.utils.validators;

import com.example.taskmanagerproject.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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

  private final Validator validator;

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
}
