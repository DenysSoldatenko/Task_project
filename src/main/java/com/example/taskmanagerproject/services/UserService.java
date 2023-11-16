package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.entities.User;

public interface UserService {

  User getUserById(Long userId);

  User getUserByUsername(String username);

  User updateUser(User user);

  User createUser(User user);

  boolean isUserTaskOwner(Long userId, Long taskId);

  void deleteUserById(Long userId);
}
