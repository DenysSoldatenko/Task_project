package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.security.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Finds a role by its name.
   *
   * @param name the name of the role (e.g., ADMIN, USER)
   * @return an Optional containing the Role if found, otherwise empty
   */
  Optional<Role> findByName(String name);

  /**
   * Checks if a role exists by its name.
   *
   * @param name the name of the role to check
   * @return true if a role with the given name exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Retrieves the role of a user within a specific team.
   *
   * @param userId the ID of the user whose role is being checked
   * @param teamId the ID of the team in which the user is a member
   * @return the role of the user in the specified team
   */
  @Query(value = """
       SELECT r.*
       FROM roles r
       JOIN teams_users tu ON tu.role_id = r.id
       WHERE tu.user_id = :userId AND tu.team_id = :teamId
       """, nativeQuery = true)
  Role getRoleForUserInTeam(@Param("userId") Long userId, @Param("teamId") Long teamId);
}
