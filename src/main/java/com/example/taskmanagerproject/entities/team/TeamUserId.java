package com.example.taskmanagerproject.entities.team;

import jakarta.persistence.Embeddable;

/**
 * Represents a composite primary key for the TeamUser entity.
 */
@Embeddable
public record TeamUserId(Long userId, Long teamId) {}
