package com.example.taskmanagerproject.repositories;

import com.example.taskmanagerproject.entities.project.ProjectTeam;
import com.example.taskmanagerproject.entities.project.ProjectTeamId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ProjectTeam entities.
 */
@Repository
public interface ProjectTeamRepository extends JpaRepository<ProjectTeam, ProjectTeamId> {

  /**
   * Finds all ProjectTeam entities by the name of the project.
   *
   * @param projectName the name of the project to filter by.
   * @return a list of ProjectTeam entities that are associated with the specified project.
   */
  @Query("SELECT pt FROM ProjectTeam pt WHERE pt.project.name = :projectName")
  List<ProjectTeam> findAllByProjectName(String projectName);

  /**
   * Finds all ProjectTeam entities associated with a specific team.
   *
   * @param teamName the name of the team to filter by.
   * @return a list of ProjectTeam entities that are associated with the specified team.
   */
  @Query("SELECT pt FROM ProjectTeam pt WHERE pt.team.name = :teamName")
  List<ProjectTeam> findAllByTeamName(String teamName);

  /**
   * Checks if a ProjectTeam exists based on the project name and team name.
   *
   * @param projectName the name of the project.
   * @param teamName the name of the team.
   * @return true if a ProjectTeam exists for the specified project and team, false otherwise.
   */
  @Query("""
      SELECT CASE WHEN COUNT(pt) > 0 THEN true ELSE false END
      FROM ProjectTeam pt
      WHERE pt.project.name = :projectName
      AND pt.team.name = :teamName
      """)
  boolean existsByProjectNameAndTeamName(String projectName, String teamName);
}
