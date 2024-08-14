package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for users in the project.
 */
@ResponseStatus(NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

  /**
   * Constructs a new UserNotFoundException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public UserNotFoundException(String message) {
    super(message);
  }
}
