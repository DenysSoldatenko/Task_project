package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Team entity.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

  /**
   * Finds a team by its name.
   *
   * @param name the name of the team
   * @return an Optional containing the Team if found, otherwise empty
   */
  Optional<Team> findByName(String name);

  /**
   * Checks if a team exists by its name.
   *
   * @param name the name of the team to check
   * @return true if a team with the given name exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Finds all teams created by a user identified by their slug.
   *
   * @param slug the slug of the user
   * @return a list of teams associated with the specified user
   */
  @Query("""
      SELECT t
      FROM Team t
      JOIN FETCH t.creator c
      WHERE c.slug = :slug
      """)
  List<Team> findByCreatorSlug(@Param("slug") String slug);
}
