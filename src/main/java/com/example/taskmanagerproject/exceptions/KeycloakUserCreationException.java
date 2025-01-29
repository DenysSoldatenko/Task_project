package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when Keycloak user creation fails.
 */
@ResponseStatus(BAD_REQUEST)
public class KeycloakUserCreationException extends RuntimeException {

  /**
   * Constructs a new KeycloakUserCreationException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public KeycloakUserCreationException(String message) {
    super(message);
  }
}
