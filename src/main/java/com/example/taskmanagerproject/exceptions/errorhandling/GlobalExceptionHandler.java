package com.example.taskmanagerproject.exceptions.errorhandling;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.example.taskmanagerproject.exceptions.ImageUploadException;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for handling specific exceptions
 * and providing consistent error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final int DESCRIPTION_START_INDEX = 4;

  /**
   * Handles the exception when a {@link TaskNotFoundException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorDetails> handlePostNotFoundException(
      final TaskNotFoundException exception,
      final WebRequest webRequest
  ) {

    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(NOT_FOUND.value()),
        NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );

    return new ResponseEntity<>(errorDetails, NOT_FOUND);
  }

  /**
   * Handles the exception when a {@link RoleNotFoundException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<ErrorDetails> handlePostNotFoundException(
      final RoleNotFoundException exception,
      final WebRequest webRequest
  ) {

    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(NOT_FOUND.value()),
        NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );

    return new ResponseEntity<>(errorDetails, NOT_FOUND);
  }

  /**
   * Handles the exception when a {@link UserNotFoundException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorDetails> handleCommentNotFoundException(
      final UserNotFoundException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(NOT_FOUND.value()),
        NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, NOT_FOUND);
  }

  /**
   * Handles the exception when a {@link ValidationException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorDetails> handleAuthException(
      final ValidationException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(BAD_REQUEST.value()),
        BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }

  /**
   * Handles the exception when a {@link ImageUploadException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(ImageUploadException.class)
  public ResponseEntity<ErrorDetails> handleImageUploadException(
      final ImageUploadException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(BAD_REQUEST.value()),
        BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }

  /**
   * Handles global exceptions that are not explicitly caught by other methods.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDetails> handleGlobalException(
      final Exception exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(INTERNAL_SERVER_ERROR.value()),
        INTERNAL_SERVER_ERROR.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles validation exceptions that occur due
   * to method argument validation failures.
   *
   * @param exception  the MethodArgumentNotValidException that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException exception,
      final WebRequest webRequest
  ) {

    String errorMessage = exception.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(joining(", "));


    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(BAD_REQUEST.value()),
        BAD_REQUEST.getReasonPhrase(),
        errorMessage,
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );

    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }

  /**
   * Handles IllegalStateException and returns a BAD_REQUEST response.
   *
   * @param exception  The IllegalStateException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error
   *     details and HTTP status BAD_REQUEST.
   */
  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(BAD_REQUEST)
  public ResponseEntity<ErrorDetails> handleIllegalStateException(
      final IllegalStateException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(BAD_REQUEST.value()),
        BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }

  /**
   * Handles AccessDeniedException and returns a FORBIDDEN response.
   *
   * @param exception  The AccessDeniedException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error
   *     details and HTTP status FORBIDDEN.
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(FORBIDDEN)
  public ResponseEntity<ErrorDetails> handleAccessDeniedException(
      final AccessDeniedException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(FORBIDDEN.value()),
        FORBIDDEN.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, FORBIDDEN);
  }

  /**
   * Handles ConstraintViolationException and returns a BAD_REQUEST response.
   *
   * @param exception  The ConstraintViolationException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error
   *     details and HTTP status BAD_REQUEST.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(BAD_REQUEST)
  public ResponseEntity<ErrorDetails> handleConstraintViolationException(
      final ConstraintViolationException exception,
      final WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        valueOf(BAD_REQUEST.value()),
        BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(DESCRIPTION_START_INDEX)
    );
    return new ResponseEntity<>(errorDetails, BAD_REQUEST);
  }
}
