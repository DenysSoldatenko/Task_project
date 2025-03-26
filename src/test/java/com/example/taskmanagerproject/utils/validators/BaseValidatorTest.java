package com.example.taskmanagerproject.utils.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.exceptions.ValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseValidatorTest {

  private TestValidator validator;

  @BeforeEach
  void setUp() {
    Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();
    validator = new TestValidator(javaxValidator);
  }

  @Test
  void validateConstraints_shouldCollectErrors() {
    DummyDto invalidDto = new DummyDto(null, "a");
    Set<String> errors = new HashSet<>();

    validator.validateConstraints(invalidDto, errors);

    assertFalse(errors.isEmpty());
    assertEquals(2, errors.size());
    assertTrue(errors.contains("Name must not be null"));
    assertTrue(errors.contains("Description must be between 5 and 100 characters"));
  }

  @Test
  void throwIfErrorsExist_shouldThrowWhenErrorsPresent() {
    Set<String> errors = Set.of("Field A required", "Invalid email");

    ValidationException exception = assertThrows(ValidationException.class, () -> validator.throwIfErrorsExist(errors));
    assertTrue(exception.getMessage().contains("Field A required"));
    assertTrue(exception.getMessage().contains("Invalid email"));
  }

  @Test
  void throwIfErrorsExist_shouldNotThrowWhenNoErrors() {
    assertDoesNotThrow(() -> validator.throwIfErrorsExist(new HashSet<>()));
  }

  // A simple concrete subclass for testing purposes
  static class TestValidator extends BaseValidator<DummyDto> {
    public TestValidator(Validator validator) {
      super(validator);
    }
  }

  // Dummy DTO with constraints
  static class DummyDto {
    @NotNull(message = "Name must not be null")
    private final String name;

    @Size(min = 5, max = 100, message = "Description must be between 5 and 100 characters")
    private final String description;

    public DummyDto(String name, String description) {
      this.name = name;
      this.description = description;
    }
  }
}
