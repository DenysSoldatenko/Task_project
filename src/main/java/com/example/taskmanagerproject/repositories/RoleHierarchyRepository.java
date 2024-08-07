package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleHierarchy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the RoleHierarchy entity.
 */
@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {

  /**
   * Finds the list of RoleHierarchy entities where the given role is the higher role.
   *
   * @param higherRole The higher role in the hierarchy.
   * @return A list of RoleHierarchy entities where the provided role is the higher role.
   */
  List<RoleHierarchy> findByHigherRole(Role higherRole);

  /**
   * Finds the list of RoleHierarchy entities where the given role is the lower role.
   *
   * @param lowerRole The lower role in the hierarchy.
   * @return A list of RoleHierarchy entities where the provided role is the lower role.
   */
  List<RoleHierarchy> findByLowerRole(Role lowerRole);
}