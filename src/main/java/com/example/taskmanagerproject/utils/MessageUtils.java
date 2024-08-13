package com.example.taskmanagerproject.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for holding application constants.
 */
@UtilityClass
public class MessageUtils {

  public static final String USER_NOT_FOUND
      = "User not found!";
  public static final String USER_NOT_FOUND_WITH_ID
      = "User not found with id: ";
  public static final String USER_NOT_FOUND_WITH_SLUG
      = "User not found with slug: ";
  public static final String USER_NOT_FOUND_WITH_USERNAME
      = "User not found with username: ";
  public static final String USER_ALREADY_EXISTS_WITH_USERNAME
      = "User already exists with username: ";
  public static final String USER_DOES_NOT_HAVE_ROLE_TO_CREATE_PROJECT
      = "The user does not have the necessary role to create a project! Username: ";
  public static final String USER_DOES_NOT_HAVE_ROLE_TO_CREATE_TEAM
      = "The user does not have the necessary role to create a team! Username: ";
  public static final String PASSWORD_MISMATCH
      = "Password and password confirmation do not match!";
  public static final String TASK_NOT_FOUND
      = "Task not found!";


  public static final String ROLE_NOT_FOUND_WITH_NAME
      = "Role not found with name: ";
  public static final String ROLE_NOT_FOUND_WITH_ID
      = "Role not found with id: ";
  public static final String ROLE_ALREADY_EXISTS
      = "Role already exists with name: ";
  public static final String ROLE_HIERARCHY_NOT_FOUND
      = "Role hierarchy not found with higher role %s and lower role %s!";


  public static final String PROJECT_NOT_FOUND_WITH_NAME
      = "Project not found with name: ";
  public static final String PROJECT_NOT_FOUND_WITH_ID
      = "Project not found with id: ";
  public static final String PROJECT_ALREADY_EXISTS
      = "Project already exists with name: ";


  public static final String TEAM_NOT_FOUND_WITH_NAME
      = "Team not found with name: ";
  public static final String TEAM_NOT_FOUND_WITH_ID
      = "Team not found with id: ";
  public static final String TEAM_ALREADY_EXISTS
      = "Team already exists with name: ";
  public static final String USER_ALREADY_IN_TEAM
      = "User with id '%s' is already a member of the team with id '%s'";


  public static final String IMAGE_UPLOAD_FAILED
      = "Image upload failed: ";
  public static final String FAILED_TO_CREATE_BUCKET
      = "Failed to create bucket: ";
  public static final String IMAGE_MUST_NOT_BE_EMPTY
      = "Image must not be empty and must have a name!";
  public static final String FAILED_TO_UPLOAD_IMAGE
      = "Failed to upload image to Minio: ";


  public static final String DATA_INITIALIZATION_SUCCESS_MESSAGE
      = "Data initialization completed successfully!";
  public static final String DATA_INITIALIZATION_FAIL_MESSAGE
      = "Data initialization completed with errors!";
}
