package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.utils.MessageUtils.PASSWORD_MISMATCH;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_ALREADY_EXISTS;
import static java.lang.String.join;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Utility class for validating user data.
 */
@Component
@Validated
@AllArgsConstructor
public class UserValidator {

  private final Validator validator;
  private final UserRepository userRepository;

  /**
   * Validates a UserDto object.
   *
   * @param userDto The UserDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateUserDto(final UserDto userDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(userDto, errorMessages);
    validateUserExists(userDto, errorMessages);
    validatePasswordMatching(userDto, errorMessages);

    if (!errorMessages.isEmpty()) {
      throw new ValidationException(join(", ", errorMessages));
    }
  }

  private void validateConstraints(final UserDto userDto, final Set<String> errorMessages) {
    Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
    for (ConstraintViolation<UserDto> violation : violations) {
      errorMessages.add(violation.getMessage());
    }
  }

  private void validatePasswordMatching(final UserDto userDto, final Set<String> errorMessages) {
    if (!userDto.password().equals(userDto.confirmPassword())) {
      errorMessages.add(PASSWORD_MISMATCH);
    }
  }

  private void validateUserExists(final UserDto userDto, final Set<String> errorMessages) {
    User existingUser = userRepository.findByUsername(userDto.username()).orElse(null);
    if (existingUser != null && !existingUser.getUsername().equals(userDto.username())) {
      errorMessages.add(USER_ALREADY_EXISTS);
    }
  }
}
