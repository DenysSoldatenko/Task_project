package com.example.taskmanagerproject.entities.users;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user entity in the project.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "full_name")
  private String fullName;

  @Email
  @Column(name = "username")
  private String username;

  @Column(name = "slug")
  private String slug;

  @Column(name = "password")
  private String password;

  @Column(name = "confirm_password")
  private String confirmPassword;

  @ManyToOne(fetch = EAGER)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Role role;

  @JsonManagedReference
  @OneToMany(mappedBy = "user")
  private List<TeamUser> teamUsers;
}
