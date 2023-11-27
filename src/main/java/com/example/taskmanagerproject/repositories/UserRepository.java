package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository {

  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);

  void update(User user);

  void create(User user);

  void insertUserRole(@Param("userId") Long userId, @Param("role") Role role);

  boolean isTaskOwner(@Param("userId") Long userId, @Param("taskId") Long taskId);

  void delete(Long id);

}