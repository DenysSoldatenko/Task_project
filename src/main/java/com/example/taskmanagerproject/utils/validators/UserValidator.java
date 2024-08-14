package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.PASSWORD_MISMATCH;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_ALREADY_EXISTS_WITH_USERNAME;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating user data.
 */
@Component
public class UserValidator extends BaseValidator<UserDto> {

  private final UserRepository userRepository;

  /**
   * Constructor for creating a UserValidator instance.
   *
   * @param validator The validator object used to validate the user.
   * @param userRepository The repository responsible for accessing user data.
   */
  public UserValidator(Validator validator, UserRepository userRepository) {
    super(validator, userRepository);
    this.userRepository = userRepository;
  }

  /**
   * Validates a UserDto object.
   *
   * @param userDto The UserDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateUserDto(UserDto userDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(userDto, errorMessages);
    validateUserExists(userDto, errorMessages);
    validatePasswordMatching(userDto, errorMessages);
    throwIfErrorsExist(errorMessages);
  }

  private void validatePasswordMatching(UserDto userDto, Set<String> errorMessages) {
    if (!userDto.password().equals(userDto.confirmPassword())) {
      errorMessages.add(PASSWORD_MISMATCH);
    }
  }

  private void validateUserExists(UserDto userDto, Set<String> errorMessages) {
    User existingUser = userRepository.findByUsername(userDto.username()).orElse(null);
    if (existingUser != null && !existingUser.getUsername().equals(userDto.username())) {
      errorMessages.add(USER_ALREADY_EXISTS_WITH_USERNAME + userDto.username());
    }
  }
}
