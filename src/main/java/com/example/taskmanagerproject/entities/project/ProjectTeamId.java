package com.example.taskmanagerproject.entities.project;

import jakarta.persistence.Embeddable;

/**
 * Represents a composite primary key for the ProjectTeam entity.
 */
@Embeddable
public record ProjectTeamId(Long teamId, Long projectId) {}
