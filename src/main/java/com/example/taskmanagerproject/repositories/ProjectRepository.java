package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  /**
   * Finds a project by its name.
   *
   * @param name the name of the project
   * @return an Optional containing the Project if found, otherwise empty
   */
  @Query("""
      SELECT p
      FROM Project p
      LEFT JOIN FETCH p.creator c
      LEFT JOIN FETCH p.projectTeams pt
      LEFT JOIN FETCH pt.team t
      LEFT JOIN FETCH t.creator tc
      LEFT JOIN FETCH tc.role r
      WHERE p.name = :name
      """)
  Optional<Project> findByName(@Param("name") String name);

  /**
   * Finds the role of a user in a specific project.
   *
   * @param projectName the name of the project.
   * @param username the username of the user.
   * @return the role of the user in the specified project.
   */
  @Query("""
      SELECT tu.role FROM TeamUser tu
      JOIN ProjectTeam pt ON tu.team.id = pt.team.id
      JOIN Project p ON pt.project.id = p.id
      WHERE p.name = :projectName AND tu.user.username = :username
      """)
  Role findRoleByProjectNameAndUsername(@Param("projectName") String projectName, @Param("username") String username);

  /**
   * Checks if a user is related to a project by being part of a team associated with the project.
   *
   * @param userId the ID of the user.
   * @param projectId the ID of the project.
   * @return true if the user is related to the project (i.e., part of a team linked to the project), false otherwise.
   */
  @Query("""
       SELECT CASE WHEN COUNT(tu) > 0 THEN TRUE ELSE FALSE END
       FROM TeamUser tu
       JOIN ProjectTeam pt ON tu.team.id = pt.team.id
       WHERE tu.user.id = :userId AND pt.project.id = :projectId
      """)
  boolean existsByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

  /**
   * Checks if a project exists by its name.
   *
   * @param name the name of the project to check
   * @return true if a project with the given name exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Finds all projects created by a user identified by their slug.
   *
   * @param slug the slug of the user
   * @return a list of projects associated with the specified user
   */
  @Query(value = """
      SELECT DISTINCT p.*
      FROM task_list.projects p
      JOIN task_list.projects_teams pt ON p.id = pt.project_id
      JOIN task_list.teams_users tu ON pt.team_id = tu.team_id
      JOIN task_list.users u ON u.id = tu.user_id
      WHERE u.slug = :slug
      """, nativeQuery = true)
  List<Project> findByUserSlug(@Param("slug") String slug);
}
