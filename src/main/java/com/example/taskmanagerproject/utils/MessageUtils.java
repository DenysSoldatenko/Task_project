package com.example.taskmanagerproject.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for holding application constants.
 */
@UtilityClass
public class MessageUtils {

  public static final String USER_NOT_FOUND
      = "User not found!";
  public static final String USER_ALREADY_EXISTS
      = "User already exists!";
  public static final String PASSWORD_MISMATCH
      = "Password and password confirmation do not match!";
  public static final String TASK_NOT_FOUND
      = "Task not found!";


  public static final String ROLE_NOT_FOUND
      = "Role not found!";
  public static final String ROLE_ALREADY_EXISTS
      = "Role already exists!";
  public static final String ROLE_NAME_NULL_OR_EMPTY
      = "Role name cannot be null or empty";
  public static final String ROLE_NAME_LENGTH_INVALID
      = "Role name must be between 3 and 50 characters.";
  public static final String ROLE_DESCRIPTION_TOO_LONG
      = "Role description must be 200 characters or less";
  public static final String ROLE_NAME_INVALID_FORMAT
      = "Role name can only contain alphanumeric characters and underscores";


  public static final String IMAGE_UPLOAD_FAILED
      = "Image upload failed: ";
  public static final String FAILED_TO_CREATE_BUCKET
      = "Failed to create bucket: ";
  public static final String IMAGE_MUST_NOT_BE_EMPTY
      = "Image must not be empty and must have a name!";
  public static final String FAILED_TO_UPLOAD_IMAGE
      = "Failed to upload image to Minio: ";
}
