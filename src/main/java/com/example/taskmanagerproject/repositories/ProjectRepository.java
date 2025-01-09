package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.projects.Project;
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
      WHERE p.name = :name
      """)
  Optional<Project> findByName(@Param("name") String name);

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
