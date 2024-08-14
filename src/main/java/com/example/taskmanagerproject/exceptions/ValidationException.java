package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for validation errors.
 */
@ResponseStatus(BAD_REQUEST)
public class ValidationException extends RuntimeException {

  /**
   * Constructs a new ValidationException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public ValidationException(String message) {
    super(message);
  }
}
