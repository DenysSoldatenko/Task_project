package com.example.taskmanagerproject.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a user entity in the project.
 */
@Data
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String fullName;

  @Email
  private String username;

  private String password;

  private String confirmPassword;

  @Column(name = "role")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "users_roles")
  @Enumerated(value = EnumType.STRING)
  private Set<Role> userRoles;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "users_tasks",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "task_id"))
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
