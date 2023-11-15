package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository {

  Optional<User> getUserById(Long userId);

  Optional<User> getUserByUsername(String username);

  void updateUser(User user);

  void createUser(User user);

  void insertUserRole(@Param("userId") Long userId, @Param("role") Role role);

  boolean isTaskOwner(@Param("userId") Long userId, @Param("taskId") Long taskId);

  void deleteUserById(Long userId);
}