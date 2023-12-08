package com.example.taskmanagerproject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be thrown when there is an issue with uploading an image.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImageUploadException extends RuntimeException {

  public ImageUploadException(final String message) {
    super(message);
  }
}
