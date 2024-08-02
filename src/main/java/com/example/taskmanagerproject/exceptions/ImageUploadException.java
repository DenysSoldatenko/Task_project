package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be thrown when there is an issue with uploading an image.
 */
@ResponseStatus(BAD_REQUEST)
public class ImageUploadException extends RuntimeException {

  /**
   * Constructs a new ImageUploadException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public ImageUploadException(final String message) {
    super(message);
  }
}
