package com.example.taskmanagerproject.exceptions;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for handling PDF generation errors.
 */
@ResponseStatus(INTERNAL_SERVER_ERROR)
public class PdfGenerationException extends RuntimeException {

  /**
   * Constructs a new PdfGenerationException with the specified message.
   *
   * @param message The error message.
   */
  public PdfGenerationException(String message) {
    super(message);
  }
}