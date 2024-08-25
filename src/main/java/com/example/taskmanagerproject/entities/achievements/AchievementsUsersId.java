package com.example.taskmanagerproject.entities.achievements;

import jakarta.persistence.Embeddable;

/**
 * Represents a composite primary key for the AchievementsUsers entity.
 */
@Embeddable
public record AchievementsUsersId(Long userId, Long achievementId) {}
