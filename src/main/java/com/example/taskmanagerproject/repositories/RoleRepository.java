package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
  Optional<Role> findByName(RoleName name);
}
