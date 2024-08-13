package com.example.taskmanagerproject.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents the association between a user, a team, and the user's role within the team.
 */
@Data
@Entity
@Table(name = "users_teams")
public class UserTeam {

  @EmbeddedId
  private UserTeamId id;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @ManyToOne
  @MapsId("teamId")
  @JsonBackReference
  @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = false)
  private Team team;

  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;
}
