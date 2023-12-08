package com.example.taskmanagerproject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for tasks in the project.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(final String message) {
    super(message);
  }
}
