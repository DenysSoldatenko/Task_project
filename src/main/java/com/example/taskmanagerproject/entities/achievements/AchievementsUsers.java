package com.example.taskmanagerproject.entities.achievements;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents the association between a user, an achievement, a team, and a project.
 */
@Data
@Entity
@Table(name = "achievements_users")
public class AchievementsUsers {

  @EmbeddedId
  private AchievementsUsersId id;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @ManyToOne
  @MapsId("achievementId")
  @JsonBackReference
  @JoinColumn(name = "achievement_id", referencedColumnName = "id", nullable = false)
  private Achievement achievement;

  @ManyToOne
  @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = false)
  private Team team;

  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
  private Project project;
}
