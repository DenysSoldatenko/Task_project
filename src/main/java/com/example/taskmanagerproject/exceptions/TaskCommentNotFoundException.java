package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class representing a not found error for task comments in the project.
 */
@ResponseStatus(NOT_FOUND)
public class TaskCommentNotFoundException extends RuntimeException {

  /**
   * Constructs a new TaskCommentNotFoundException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public TaskCommentNotFoundException(String message) {
    super(message);
  }
}
