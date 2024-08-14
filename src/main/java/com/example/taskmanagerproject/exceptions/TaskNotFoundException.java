package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for tasks in the project.
 */
@ResponseStatus(NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

  /**
   * Constructs a new TaskNotFoundException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public TaskNotFoundException(String message) {
    super(message);
  }
}
