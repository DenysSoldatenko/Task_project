package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  @Override
  public User getUserById(Long userId) {
    return null;
  }

  @Override
  public User getUserByUsername(String username) {
    return null;
  }

  @Override
  public User updateUser(User user) {
    return null;
  }

  @Override
  public User createUser(User user) {
    return null;
  }

  @Override
  public boolean isUserTaskOwner(Long userId, Long taskId) {
    return false;
  }

  @Override
  public void deleteUserById(Long userId) {

  }
}
