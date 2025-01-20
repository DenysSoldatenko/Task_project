package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.entities.users.Role;
import java.util.List;

/**
 * Service interface for managing projects.
 * Provides methods for handling project-related operations such as creating a project.
 */
public interface ProjectService {

  /**
   * Creates a new project.
   *
   * @param projectDto The data transfer object containing the project information.
   *                  This object includes details like project name, description, and creator.
   * @return A {@link ProjectDto} representing the created project.
   */
  ProjectDto createProject(ProjectDto projectDto);

  /**
   * Retrieves a project by its name.
   *
   * @param name The name of the project to retrieve.
   * @return A {@link ProjectDto} representing the project with the given name.
   *         This will contain the project details as it exists in the system.
   */
  ProjectDto getProjectByName(String name);

  /**
   * Updates an existing project based on the provided project data.
   *
   * @param projectName The name of the project to be updated.
   * @param projectDto The data transfer object containing the updated project information.
   *                  This object includes details like updated project name, description, etc.
   * @return A {@link ProjectDto} representing the updated project.
   */
  ProjectDto updateProject(String projectName, ProjectDto projectDto);

  /**
   * Deletes the project with the specified ID.
   *
   * @param projectName The name of the project to be deleted.
   */
  void deleteProject(String projectName);

  /**
   * Retrieves all projects associated with a user by their slug.
   *
   * @param slug the unique identifier (username) of the user
   * @return a list of ProjectDto objects associated with the specified user
   */
  List<ProjectDto> getProjectsBySlug(String slug);

  /**
   * Adds a list of teams to a specific project, assigning them roles.
   *
   * @param projectName the name of the project to which the teams will be added
   * @param projectTeamDtoList the list of team-project-role associations to be added
   * @return the updated ProjectDto object after the teams have been added to the project
   */
  ProjectDto addTeamToProject(String projectName, List<ProjectTeamDto> projectTeamDtoList);

  /**
   * Retrieves all teams associated with a specific project.
   *
   * @param projectName the name of the project to get the associated teams for
   * @return a list of teams associated with the project
   */
  List<ProjectTeamDto> getTeamsForProject(String projectName);

  /**
   * Retrieves all projects associated with a specific team.
   *
   * @param teamName the name of the team to get the associated projects for
   * @return a list of projects associated with the team
   */
  List<ProjectTeamDto> getProjectsForTeam(String teamName);

  /**
   * Retrieves the role of a user in a specific project by the project name and the user's username.
   *
   * @param name the name of the project to check the user's role for.
   * @param username the username of the user to check the role for.
   * @return the {@link Role} of the user within the specified project.
   */
  Role getRoleByProjectNameAndUsername(String name, String username);

  /**
   * Checks if a user is related to a project by being part of a team associated with the project.
   *
   * @param userId the ID of the user to check.
   * @param projectId the ID of the project to check.
   * @return true, if the user is related to the project (in other words, is part of a team linked to the project), false otherwise.
   */
  boolean existsByUserIdAndProjectId(Long userId, Long projectId);
}
