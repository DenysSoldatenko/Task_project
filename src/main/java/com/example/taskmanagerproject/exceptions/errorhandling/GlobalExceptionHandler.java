package com.example.taskmanagerproject.exceptions.errorhandling;

import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
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

  /**
   * Handles the exception when a {@link TaskNotFoundException} occurs.
   *
   * @param exception  the exception that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorDetails> handlePostNotFoundException(
      TaskNotFoundException exception, WebRequest webRequest
  ) {

    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.NOT_FOUND.value()),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );

    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
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
      UserNotFoundException exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.NOT_FOUND.value()),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
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
      ValidationException exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
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
      Exception exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles validation exceptions that occur due to method argument validation failures.
   *
   * @param exception  the MethodArgumentNotValidException that was thrown.
   * @param webRequest the web request where the exception occurred.
   * @return a ResponseEntity containing details of the error response.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception, WebRequest webRequest
  ) {

    String errorMessage = exception.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));


    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        errorMessage,
        webRequest.getDescription(false).substring(4)
    );

    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles IllegalStateException and returns a BAD_REQUEST response.
   *
   * @param exception  The IllegalStateException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error details and HTTP status BAD_REQUEST.
   */
  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorDetails> handleIllegalStateException(
      IllegalStateException exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles AccessDeniedException and returns a FORBIDDEN response.
   *
   * @param exception  The AccessDeniedException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error details and HTTP status FORBIDDEN.
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorDetails> handleAccessDeniedException(
      AccessDeniedException exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.FORBIDDEN.value()),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles ConstraintViolationException and returns a BAD_REQUEST response.
   *
   * @param exception  The ConstraintViolationException to handle.
   * @param webRequest The WebRequest associated with the exception.
   * @return A ResponseEntity containing the error details and HTTP status BAD_REQUEST.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorDetails> handleConstraintViolationException(
      ConstraintViolationException exception, WebRequest webRequest
  ) {
    ErrorDetails errorDetails = new ErrorDetails(
        new Date(),
        String.valueOf(HttpStatus.BAD_REQUEST.value()),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        webRequest.getDescription(false).substring(4)
    );
    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }
}