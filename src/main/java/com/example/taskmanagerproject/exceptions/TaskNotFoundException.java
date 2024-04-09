package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for tasks in the project.
 */
@ResponseStatus(NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(final String message) {
    super(message);
  }
}
