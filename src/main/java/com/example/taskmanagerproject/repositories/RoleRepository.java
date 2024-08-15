package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.security.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
