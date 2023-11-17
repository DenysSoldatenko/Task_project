package com.example.taskmanagerproject.entities;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * Represents a user entity in the project.
 */
@Data
public class User {

  private Long id;
  private String fullName;
  private String username;
  private String password;
  private String confirmPassword;
  private Set<Role> userRoles;
  private List<Task> userTasks;

  @ToString.Include(name = "password")
  private String maskPassword() {
    return "********";
  }

  @ToString.Include(name = "confirmPassword")
  private String maskConfirmPassword() {
    return "********";
  }
}
