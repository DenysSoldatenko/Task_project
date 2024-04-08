package com.example.taskmanagerproject.entities;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.io.Serializable;
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
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String fullName;

  @Email
  private String username;

  private String password;

  private String confirmPassword;

  @Column(name = "role")
  @Enumerated(value = STRING)
  @ElementCollection(fetch = EAGER)
  @CollectionTable(name = "users_roles")
  private Set<Role> userRoles;

  @OneToMany(fetch = EAGER)
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
