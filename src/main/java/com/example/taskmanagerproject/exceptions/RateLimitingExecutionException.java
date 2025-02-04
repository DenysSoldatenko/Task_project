package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an error occurs during execution of a rate-limited operation.
 */
@ResponseStatus(INTERNAL_SERVER_ERROR)
public class RateLimitingExecutionException extends RuntimeException {

  /**
   * Constructs a new RateLimitingExecutionException with the specified detail message and cause.
   *
   * @param message The detail message explaining the execution error.
   * @param cause   The underlying cause of the exception.
   */
  public RateLimitingExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}