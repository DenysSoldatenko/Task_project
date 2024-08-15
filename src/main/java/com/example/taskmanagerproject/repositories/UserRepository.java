package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.security.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a User by username and fetches the associated Role.
   *
   * @param username The username of the user.
   * @return An Optional containing the User if found, or empty if not.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.username = :username")
  Optional<User> findByUsername(String username);

  /**
   * Finds a User by slug and fetches the associated Role.
   *
   * @param slug The slug of the user.
   * @return An Optional containing the User if found, or empty if not.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.slug = :slug")
  Optional<User> findBySlug(String slug);

  @Query(value = """
            SELECT u.id,
                   u.full_name,
                   u.username,
                   u.password,
                   u.confirm_password
            FROM users_tasks ut
                     JOIN users u ON ut.user_id = u.id
            WHERE ut.task_id = :taskId
            """, nativeQuery = true)
  Optional<User> findTaskAuthorByTaskId(@Param("taskId") Long taskId);

  @Query(value = """
            SELECT exists(SELECT 1
                          FROM users_tasks
                          WHERE user_id = :userId
                            AND task_id = :taskId)
            """, nativeQuery = true)
  boolean isTaskOwner(@Param("userId") Long userId, @Param("taskId") Long taskId);


  /**
   * Checks if a given user is the creator of a specific project.
   *
   * @param projectName the name of the project
   * @param username the username of the user
   * @return true if the user is the creator of the project, false otherwise
   */
  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM Project p
      WHERE p.name = :projectName AND p.creator.username = :username
      """)
  boolean isProjectCreator(@Param("projectName") String projectName, @Param("username") String username);

  /**
   * Checks if a given user is the creator of a specific team.
   *
   * @param teamName the name of the team
   * @param username the username of the user
   * @return true if the user is the creator of the team, false otherwise
   */
  @Query("""
      SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
      FROM Team t
      WHERE t.name = :teamName AND t.creator.username = :username
      """)
  boolean isTeamCreator(@Param("teamName") String teamName, @Param("username") String username);

  /**
   * Checks if the given user has one of the specified roles (ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD)
   * in the context of the given team.
   *
   * @param teamName the name of the team to check the user's roles for
   * @param username the username of the user to check
   * @return true if the user has one of the specified roles in the given team, false otherwise
   */
  @Query(value = """
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM users u
      JOIN teams_users ut ON u.id = ut.user_id
      JOIN roles r ON r.id = ut.role_id
      WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'MANAGER', 'TEAM_LEAD')
      AND u.username = :username
      AND ut.team_id = (SELECT t.id FROM teams t WHERE t.name = :teamName)
      """, nativeQuery = true)
  boolean isUserInLeadershipPositionInTeam(@Param("teamName") String teamName, @Param("username") String username);

  /**
   * Checks if the given user has one of the specified roles (ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD).
   *
   * @param username the username of the user to check
   * @return true if the user has one of the specified roles in the given team, false otherwise
   */
  @Query(value = """
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM users u
               JOIN users_roles ur on u.id = ur.user_id
               JOIN roles r ON r.id = ur.role_id
      WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'MANAGER', 'TEAM_LEAD')
      AND u.username = :username
      """, nativeQuery = true)
  boolean isUserInLeadershipPosition(@Param("username") String username);
}
