package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a general resource not found error.
 */
@ResponseStatus(NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Constructs a new ResourceNotFoundException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
