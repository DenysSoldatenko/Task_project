package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Achievement entity.
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

  /**
   * Finds all achievements for a given user, team, and project using a native SQL query.
   *
   * @param userId   the user ID
   * @param teamId   the team ID
   * @param projectId the project ID
   * @return a list of achievements
   */
  @Query(value = """
      SELECT a.*
      FROM achievements a
      JOIN achievements_users ua ON ua.achievement_id = a.id
      JOIN users u ON ua.user_id = u.id
      JOIN teams t ON ua.team_id = t.id
      JOIN projects p ON ua.project_id = p.id
      WHERE ua.user_id = :userId
      AND ua.team_id = :teamId
      AND ua.project_id = :projectId
      """, nativeQuery = true)
  List<Achievement> findAchievementsByUserTeamAndProject(Long userId, Long teamId, Long projectId);
}
