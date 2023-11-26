package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public User getUserById(Long userId) {
    return userRepository.getUserById(userId)
      .orElseThrow(() -> new UserNotFoundException("User not found."));
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserByUsername(String username) {
    return userRepository.getUserByUsername(username)
      .orElseThrow(() -> new UserNotFoundException("User not found."));
  }

  @Override
  @Transactional
  public User updateUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.updateUser(user);
    return user;
  }

  @Override
  @Transactional
  public User createUser(User user) {
    if (userRepository.getUserByUsername(user.getUsername()).isPresent()) {
      throw new IllegalStateException("User already exists.");
    }
    if (!user.getPassword().equals(user.getConfirmPassword())) {
      throw new IllegalStateException("Password and password confirmation do not match.");
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.createUser(user);
    Set<Role> roles = Set.of(Role.ROLE_USER);
    userRepository.insertUserRole(user.getId(), Role.ROLE_USER);
    user.setUserRoles(roles);
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isUserTaskOwner(Long userId, Long taskId) {
    return userRepository.isTaskOwner(userId, taskId);
  }

  @Override
  @Transactional
  public void deleteUserById(Long userId) {
    userRepository.deleteUserById(userId);
  }
}
