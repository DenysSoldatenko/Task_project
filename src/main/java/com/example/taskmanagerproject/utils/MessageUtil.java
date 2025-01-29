package com.example.taskmanagerproject.utils;

import lombok.experimental.UtilityClass;

/**
 * Utility class for holding application constants.
 */
@UtilityClass
public class MessageUtil {

  public static final String USER_NOT_FOUND_WITH_SLUG
      = "User not found with slug: ";
  public static final String USER_NOT_FOUND_WITH_USERNAME
      = "User not found with username: ";
  public static final String USER_ALREADY_EXISTS_WITH_USERNAME
      = "User already exists with username: ";
  public static final String USER_ALREADY_IN_TEAM
      = "User with id '%s' is already a member of the team with id '%s'";
  public static final String USERS_NOT_IN_SAME_TEAM
      = "Users '%s' and '%s' must belong to the same team to assign tasks";
  public static final String USERS_NOT_IN_SAME_PROJECT
      = "Users '%s' and '%s' must belong to the same project '%s' to assign tasks";
  public static final String USERS_DO_NOT_HAVE_ROLES_IN_TEAM
      = "One or both users do not have roles in the specified team";
  public static final String USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_PROJECT
      = "The user does not have the necessary role to create or update a project! Username: ";
  public static final String USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM
      = "The user does not have the necessary role to create or update a team! Username: ";
  public static final String PASSWORD_MISMATCH
      = "Password and password confirmation do not match!";

  public static final String TASK_NOT_FOUND_WITH_ID
      = "Task not found with id: ";
  public static final String TASK_COMMENT_INVALID_SENDER_RECEIVER
      = "Sender and receiver combination is invalid";
  public static final String TASK_COMMENT_FOUND_WITH_ID
      = "Task comment not found with id: ";
  public static final String ERROR_EXPIRATION_IN_PAST
      = "Expiration date cannot be in the past";

  public static final String USER_NOT_IN_TEAM
      = "User is not part of the specified team";
  public static final String TEAM_NOT_IN_PROJECT
      = "Team is not part of the specified project";
  public static final String DATE_RANGE_REQUIRED
      = "Start date and end date must be provided";
  public static final String INVALID_DATE_RANGE
      = "Invalid date range: Start date must be before end date";

  public static final String TEMPLATE_LOAD_ERROR
      = "Failed to load HTML template from file: ";
  public static final String PDF_GENERATION_ERROR
      = "Failed to generate PDF.";
  public static final String TASK_METRICS_NOT_FOUND_ERROR
      = "No task metrics found for user %s in project %s in the date range from %s to %s.";
  public static final String TEAM_PERFORMANCE_NOT_FOUND_ERROR
      = "No team performance metrics found for team %s in the date range from %s to %s.";
  public static final String PROJECT_PERFORMANCE_NOT_FOUND_ERROR
      = "No project performance metrics found for project %s in the date range from %s to %s.";

  public static final String ROLE_NOT_FOUND_WITH_NAME
      = "Role not found with name: ";
  public static final String ROLE_ALREADY_EXISTS
      = "Role already exists with name: ";
  public static final String ROLE_HIERARCHY_NOT_FOUND
      = "Role hierarchy not found with higher role %s and lower role %s!";
  public static final String ROLE_DISCREPANCY_FOUND
      = "Discrepancy found between roles: the higher role %s cannot be assigned to the lower role %s!";

  public static final String PROJECT_NOT_FOUND_WITH_NAME
      = "Project not found with name: ";
  public static final String PROJECT_ALREADY_EXISTS
      = "Project already exists with name: ";


  public static final String TEAM_NOT_FOUND_WITH_NAME
      = "Team not found with name: ";
  public static final String TEAM_ALREADY_EXISTS
      = "Team already exists with name: ";

  public static final String NO_IMAGE_TO_UPDATE
      = "No image to update.";
  public static final String NO_IMAGE_TO_DELETE
      = "No image to delete.";
  public static final String FAILED_TO_DELETE_IMAGE
      = "Failed to delete image: %s. %s";
  public static final String FAILED_TO_CREATE_BUCKET
      = "Failed to create bucket: ";
  public static final String IMAGE_MUST_NOT_BE_EMPTY
      = "Image must not be empty and must have a name!";
  public static final String FAILED_TO_UPLOAD_IMAGE
      = "Failed to upload image to Minio: ";
  public static final String IMAGE_DOWNLOAD_ERROR
      = "Failed to download image: ";

  public static final String DATA_INITIALIZATION_SUCCESS_MESSAGE
      = "Data initialization completed successfully!";
  public static final String DATA_INITIALIZATION_FAIL_MESSAGE
      = "Data initialization completed with errors!";
  public static final String RATE_LIMIT_EXCEEDED
      = "Request rate exceeded. Please slow down and try again shortly.";


  public static final String KEYCLOAK_ERROR_FAILED_TO_CREATE_USER
      = "Failed to create user in Keycloak. Status: %d. Details: %s";
  public static final String KEYCLOAK_ERROR_GENERIC_CREATION
      = "An error occurred during Keycloak user creation for %s. Details: %s";

}
