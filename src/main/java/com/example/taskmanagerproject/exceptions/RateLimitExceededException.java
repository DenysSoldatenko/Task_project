package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the rate limit for a request is exceeded.
 */
@ResponseStatus(TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

  /**
   * Constructs a new RateLimitExceededException with the specified detail message.
   *
   * @param message The detail message explaining the rate limit error.
   */
  public RateLimitExceededException(String message) {
    super(message);
  }
}