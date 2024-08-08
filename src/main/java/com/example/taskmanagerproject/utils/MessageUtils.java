package com.example.taskmanagerproject.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for holding application constants.
 */
@UtilityClass
public class MessageUtils {

  public static final String USER_NOT_FOUND
      = "User not found!";
  public static final String USER_NOT_FOUND_WITH_SLUG
      = "User not found with slug: ";
  public static final String USER_NOT_FOUND_WITH_USERNAME
      = "User not found with username: ";
  public static final String USER_ALREADY_EXISTS_WITH_USERNAME
      = "User already exists with username: ";
  public static final String PASSWORD_MISMATCH
      = "Password and password confirmation do not match!";
  public static final String TASK_NOT_FOUND
      = "Task not found!";


  public static final String ROLE_NOT_FOUND_WITH_NAME
      = "Role not found with name: ";
  public static final String ROLE_ALREADY_EXISTS
      = "Role already exists with name: ";
  public static final String ROLE_HIERARCHY_NOT_FOUND
      = "Role hierarchy not found with higher role %s and lower role %s!";

  public static final String IMAGE_UPLOAD_FAILED
      = "Image upload failed: ";
  public static final String FAILED_TO_CREATE_BUCKET
      = "Failed to create bucket: ";
  public static final String IMAGE_MUST_NOT_BE_EMPTY
      = "Image must not be empty and must have a name!";
  public static final String FAILED_TO_UPLOAD_IMAGE
      = "Failed to upload image to Minio: ";
}
