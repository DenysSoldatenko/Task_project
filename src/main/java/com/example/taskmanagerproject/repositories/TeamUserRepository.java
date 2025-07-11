package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing UserTeam entities.
 */
@Repository
public interface TeamUserRepository extends JpaRepository<TeamUser, TeamUserId> {

  /**
   * Finds all TeamUser entities by the name of the team.
   *
   * @param teamName the name of the team to filter by.
   * @return a list of TeamUser entities that are associated with the specified team.
   */
  @Query("SELECT tu FROM TeamUser tu WHERE tu.team.name = :teamName")
  List<TeamUser> findAllByTeamName(String teamName);

  /**
   * Finds the role of a user in a specific team.
   *
   * @param teamName the name of the team.
   * @param username the username of the user.
   * @return the role of the user in the specified team.
   */
  @Query("SELECT tu.role FROM TeamUser tu WHERE tu.team.name = :teamName AND tu.user.username = :username")
  Role findRoleByTeamNameAndUsername(String teamName, String username);

  /**
   * Finds a random user within the same team who has a higher role than the user with the given teamId and userId.
   * The role hierarchy is checked to ensure the user found has a role considered higher in the hierarchy.
   *
   * @param teamId the ID of the team where the user belongs.
   * @param userId the ID of the user whose role will be used to filter the other users.
   * @return an {@link Optional} containing the ID of a random user with a higher role, or an empty {@link Optional} if no such user exists.
   */
  @Query(value = """
        SELECT u.id
        FROM task_list.users u
        WHERE u.id IN (
          SELECT tu.user_id
          FROM task_list.teams_users tu
          WHERE tu.team_id = :teamId
            AND EXISTS (
              SELECT 1
              FROM task_list.role_hierarchy rh
              WHERE rh.higher_role = tu.role_id
                AND rh.lower_role = (
                  SELECT tu2.role_id
                  FROM task_list.teams_users tu2
                  WHERE tu2.team_id = :teamId
                    AND tu2.user_id = :userId
                )
            )
        )
        AND u.id != :userId
        ORDER BY RANDOM()
        LIMIT 1;
        """, nativeQuery = true)
  Optional<Long> findRandomHigherRoleUser(Long teamId, Long userId);

  /**
   * Checks if a UserTeam exists based on user and team using a native query.
   *
   * @param userId The user's ID.
   * @param teamId The team's ID.
   * @return True if a UserTeam exists, otherwise False.
   */
  @Query("""
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM TeamUser tu
      WHERE tu.user.id = :userId AND tu.team.id = :teamId
      """)
  boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
