package com.example.taskmanagerproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

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

  @ManyToOne(fetch = EAGER)
  @JoinTable(name = "users_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Role role;

  @OneToMany(mappedBy = "user")
  private List<UserTask> userTasks;

  @OneToMany(mappedBy = "user")
  private List<UserTeam> userTeams;
}
