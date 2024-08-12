package com.example.taskmanagerproject.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the association between a user, a team, and the user's role within the team.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_teams")
public class UserTeam {

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Id
  @ManyToOne
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;
}
