package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.achievements.AchievementsUsers;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Achievement entity.
 */
@Repository
public interface AchievementsUsersRepository extends JpaRepository<AchievementsUsers, AchievementsUsersId> {

  /**
   * Checks if an achievement exists for a given user, team, and project.
   *
   * @param userId   the user ID
   * @param teamId   the team ID
   * @param projectId the project ID
   * @return true if an achievement exists, false otherwise
   */
  boolean existsByUserIdAndTeamIdAndProjectIdAndAchievementId(Long userId, Long teamId, Long projectId, Long achievementId);
}
