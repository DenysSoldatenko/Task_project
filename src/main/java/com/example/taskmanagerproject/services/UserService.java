package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;

/**
 * Service interface for managing users.
 */
public interface UserService {

  UserDto getUserBySlug(String slug);

  User getUserByUsername(String username);

  UserDto updateUser(UserDto user, String slug);

  User createUser(UserDto user);

  boolean isUserTaskOwner(Long userId, Long taskId);

  UserDto getTaskAuthor(Long taskId);

  void deleteUserBySlug(String slug);
}

