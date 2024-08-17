package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.security.UserDto;
import com.example.taskmanagerproject.entities.security.User;

/**
 * Service interface for managing users.
 */
public interface UserService {

  /**
   * Retrieves a UserDto by the user's slug.
   *
   * @param slug the slug of the user
   * @return the UserDto associated with the given slug
   */
  UserDto getUserBySlug(String slug);

  /**
   * Retrieves a User entity by the user's username.
   *
   * @param username the username of the user
   * @return the User entity associated with the given username
   */
  User getUserByUsername(String username);

  /**
   * Updates an existing user.
   *
   * @param user the updated user data
   * @param slug the slug of the user to update
   * @return the updated UserDto
   */
  UserDto updateUser(UserDto user, String slug);

  /**
   * Creates a new user.
   *
   * @param user the user data to create
   * @return the created User entity
   */
  User createUser(UserDto user);

  /**
   * Checks if a user is the owner of a specific task.
   *
   * @param userId the ID of the user
   * @param taskId the ID of the task
   * @return true if the user is the owner of the task, false otherwise
   */
  boolean isUserTaskOwner(Long userId, Long taskId);

  /**
   * Deletes a user by their slug.
   *
   * @param slug the slug of the user to delete
   */
  void deleteUserBySlug(String slug);

  /**
   * Checks if a user is the creator of a specific project.
   *
   * @param projectName the name of the project
   * @param username the username of the user
   * @return true if the user is the creator of the project, false otherwise
   */
  boolean isProjectCreator(String projectName, String username);

  /**
   * Checks if the user has access to a specific team.
   *
   * @param teamName the name of the team to check access for
   * @param username the username of the user whose access status is being checked
   * @return true if the user has access to the team, false otherwise
   */
  boolean hasTeamAccess(String teamName, String username);

  /**
   * Checks if the user is assigned to a specific task.
   *
   * @param id the ID of the user whose assignment status is being checked
   * @param taskId the ID of the task to check the user's assignment for
   * @return true if the user is assigned to the task, false otherwise
   */
  boolean isUserAssignedToTask(Long id, Long taskId);
}
