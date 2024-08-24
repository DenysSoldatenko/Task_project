package com.example.taskmanagerproject.entities.projects;

import com.example.taskmanagerproject.entities.teams.Team;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents the association between a team, a project, and the team's role within the project.
 */
@Data
@Entity
@Table(name = "projects_teams")
public class ProjectTeam {

  @EmbeddedId
  private ProjectTeamId id;

  @ManyToOne
  @MapsId("teamId")
  @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = false)
  private Team team;

  @ManyToOne
  @MapsId("projectId")
  @JsonBackReference
  @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
  private Project project;
}
