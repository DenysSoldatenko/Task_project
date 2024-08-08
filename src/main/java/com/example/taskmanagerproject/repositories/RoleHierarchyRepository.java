package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleHierarchy;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the RoleHierarchy entity.
 */
@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {

  /**
   * Finds the list of RoleHierarchy entities where the given role is the higher role.
   * The related roles will be eagerly fetched in the same query using JOIN FETCH.
   *
   * @param higherRole The higher role in the hierarchy.
   * @return A list of RoleHierarchy entities where the provided role is the higher role.
   */
  @Query("""
      SELECT rh FROM RoleHierarchy rh
      JOIN FETCH rh.higherRole
      WHERE rh.higherRole = :higherRole
      """)
  List<RoleHierarchy> findByHigherRole(Role higherRole);

  /**
   * Finds the list of RoleHierarchy entities where the given role is the lower role.
   * The related roles will be eagerly fetched in the same query using JOIN FETCH.
   *
   * @param lowerRole The lower role in the hierarchy.
   * @return A list of RoleHierarchy entities where the provided role is the lower role.
   */
  @Query("""
      SELECT rh FROM RoleHierarchy rh
      JOIN FETCH rh.lowerRole
      WHERE rh.lowerRole = :lowerRole
      """)
  List<RoleHierarchy> findByLowerRole(Role lowerRole);

  /**
   * Finds all RoleHierarchy entities and eagerly fetches related roles using JOIN FETCH.
   *
   * @return A list of all RoleHierarchy entities.
   */
  @NotNull
  @Query("""
        SELECT rh FROM RoleHierarchy rh
        JOIN FETCH rh.higherRole
        JOIN FETCH rh.lowerRole
      """)
  List<RoleHierarchy> findAll();
}
