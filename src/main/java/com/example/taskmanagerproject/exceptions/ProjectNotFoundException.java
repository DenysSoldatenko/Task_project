package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for projects in the project.
 */
@ResponseStatus(NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException {

  /**
   * Constructs a new ProjectNotFoundException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public ProjectNotFoundException(final String message) {
    super(message);
  }
}