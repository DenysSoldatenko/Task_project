package com.example.taskmanagerproject.entities;

import jakarta.persistence.Embeddable;

/**
 * Represents a composite primary key for the UserTeam entity.
 */
@Embeddable
public record UserTeamId(Long userId, Long teamId) {}
