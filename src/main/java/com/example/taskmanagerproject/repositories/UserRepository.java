package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.users.User;
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
  @Query("SELECT u FROM User u JOIN FETCH u.teamUsers t WHERE u.username = :username")
  Optional<User> findByUsername(String username);

  /**
   * Finds a User by slug and fetches the associated Role.
   *
   * @param slug The slug of the user.
   * @return An Optional containing the User if found, or empty if not.
   */
  @Query("SELECT u FROM User u LEFT JOIN FETCH u.teamUsers t WHERE u.slug = :slug")
  Optional<User> findBySlug(String slug);

  /**
   * Checks if the given user is the owner (assigned by) of the specified task.
   *
   * @param userId The ID of the user to check.
   * @param taskId The ID of the task to check.
   * @return true if the user is the owner of the task, false otherwise.
   */
  @Query("""
      SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
      FROM Task t
      WHERE t.assignedBy.id = :userId
      AND t.id = :taskId
      """)
  boolean isTaskOwner(@Param("userId") Long userId, @Param("taskId") Long taskId);

  /**
   * Checks if the given user is assigned to the specified task.
   *
   * @param userId The ID of the user to check.
   * @param taskId The ID of the task to check.
   * @return true if the user is assigned to the task, false otherwise.
   */
  @Query("""
      SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
      FROM Task t
      WHERE t.assignedTo.id = :userId
      AND t.id = :taskId
      """)
  boolean isUserAssignedToTask(@Param("userId") Long userId, @Param("taskId") Long taskId);

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
      FROM task_list.users u
      JOIN task_list.teams_users ut ON u.id = ut.user_id
      JOIN task_list.roles r ON r.id = ut.role_id
      WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'MANAGER', 'TEAM_LEAD')
      AND u.username = :username
      AND ut.team_id = (SELECT t.id FROM task_list.teams t WHERE t.name = :teamName)
      """, nativeQuery = true)
  boolean isUserInLeadershipPositionInTeam(@Param("teamName") String teamName, @Param("username") String username);

  /**
   * Checks if the given user has one of the specified roles (ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, PROJECT_LEAD)
   * in the context of the given project.
   *
   * @param projectName the name of the project to check the user's roles for
   * @param username the username of the user to check
   * @return true if the user has one of the specified roles in the given project, false otherwise
   */
  @Query(value = """
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM task_list.users u
      JOIN task_list.teams_users tu ON u.id = tu.user_id
      JOIN task_list.projects_teams pt ON tu.team_id = pt.team_id
      JOIN task_list.roles r ON r.id = tu.role_id
      WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'MANAGER', 'PROJECT_LEAD')
      AND u.username = :username
      AND pt.project_id = (SELECT p.id FROM task_list.projects p WHERE p.name = :projectName)
      """, nativeQuery = true)
  boolean isUserInLeadershipPositionInProject(@Param("projectName") String projectName, @Param("username") String username);

  /**
   * Checks if the given user has one of the specified roles (ADMIN, PRODUCT_OWNER, SCRUM_MASTER, MANAGER, TEAM_LEAD).
   *
   * @param username the username of the user to check
   * @return true if the user has one of the specified roles in the given team, false otherwise
   */
  @Query(value = """
      SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
      FROM task_list.users u
               JOIN task_list.teams_users tr on u.id = tr.user_id
               JOIN task_list.roles r ON r.id = tr.role_id
      WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'MANAGER', 'TEAM_LEAD')
      AND u.username = :username
      """, nativeQuery = true)
  boolean isUserInLeadershipPosition(@Param("username") String username);
}
