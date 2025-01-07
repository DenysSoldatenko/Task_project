package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * General Exception to be thrown for image processing errors.
 */
@ResponseStatus(BAD_REQUEST)
public class ImageProcessingException extends RuntimeException {

  /**
   * Constructs a new ImageUploadException with the specified detail message.
   *
   * @param message The detail message explaining the error.
   */
  public ImageProcessingException(String message) {
    super(message);
  }
}
