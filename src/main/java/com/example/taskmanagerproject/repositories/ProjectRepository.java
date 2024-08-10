package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Project;
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
  Optional<Project> findByName(String name);

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
  @Query("""
      SELECT p
      FROM Project p
      JOIN FETCH p.creator c
      WHERE c.slug = :slug
      """)
  List<Project> findByCreatorSlug(@Param("slug") String slug);
}
