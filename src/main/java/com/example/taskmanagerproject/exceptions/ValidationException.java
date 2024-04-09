package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for validation errors.
 */
@ResponseStatus(BAD_REQUEST)
public class ValidationException extends RuntimeException {
  public ValidationException(final String message) {
    super(message);
  }
}
