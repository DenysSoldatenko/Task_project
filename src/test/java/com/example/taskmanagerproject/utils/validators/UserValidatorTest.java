package com.example.taskmanagerproject.utils.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserValidatorTest {

  private Validator mockValidator;
  private UserValidator userValidator;
  private UserRepository mockUserRepository;

  @BeforeEach
  void setUp() {
    mockValidator = mock(Validator.class);
    mockUserRepository = mock(UserRepository.class);
    userValidator = new UserValidator(mockValidator, mockUserRepository);
  }

  private UserDto sampleUserDto() {
    return new UserDto(1L, "Test User", "test@example.com", "test-slug", "", Collections.emptyList());
  }

  @Test
  void validateUserDto_shouldPassWhenNoViolationsAndUsernameIsNew() {
    UserDto userDto = sampleUserDto();

    when(mockValidator.validate(userDto)).thenReturn(Collections.emptySet());
    when(mockUserRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> userValidator.validateUserDto(userDto));
  }

  @Test
  void validateUserDto_shouldThrowIfConstraintViolationsExist() {
    UserDto userDto = sampleUserDto();

    ConstraintViolation<UserDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Email must be valid");

    when(mockValidator.validate(userDto)).thenReturn(Set.of(violation));
    when(mockUserRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());

    ValidationException exception = assertThrows(ValidationException.class, () -> userValidator.validateUserDto(userDto));

    assertTrue(exception.getMessage().contains("Email must be valid"));
  }

  @Test
  void validateUserDto_shouldPassWhenUsernameMatchesExistingUserExactly() {
    UserDto userDto = sampleUserDto();

    User existingUser = new User();
    existingUser.setUsername("test@example.com");

    when(mockValidator.validate(userDto)).thenReturn(Collections.emptySet());
    when(mockUserRepository.findByUsername("test@example.com")).thenReturn(Optional.of(existingUser));

    assertDoesNotThrow(() -> userValidator.validateUserDto(userDto));
  }
}
