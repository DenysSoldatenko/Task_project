package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.dtos.UserTeamDto;
import java.util.List;

/**
 * Service interface for managing teams.
 */
public interface TeamService {

  /**
   * Creates a new team.
   *
   * @param teamDto the data transfer object containing team details
   * @return the created team data
   */
  TeamDto createTeam(TeamDto teamDto);

  /**
   * Retrieves a team by its name.
   *
   * @param teamName the name of the team
   * @return the team data if found
   */
  TeamDto getTeamByName(String teamName);

  /**
   * Updates an existing team.
   *
   * @param teamName the name of the team to update
   * @param teamDto the data transfer object containing updated team details
   * @return the updated team data
   */
  TeamDto updateTeam(String teamName, TeamDto teamDto);

  /**
   * Deletes a team by its name.
   *
   * @param teamName the name of the team to delete
   */
  void deleteTeam(String teamName);

  /**
   * Retrieves all teams associated with a user by their slug.
   *
   * @param slug the unique identifier (username) of the user
   * @return a list of TeamDto objects associated with the specified user
   */
  List<TeamDto> getTeamsBySlug(String slug);

  /**
   * Adds users to a specific team based on the team name and a list of UserTeamDto objects.
   *
   * @param teamName the name of the team to which users will be added
   * @param userTeamDtoList a list of UserTeamDto objects
   *                        containing user-role associations for the team
   * @return the updated TeamDto object after the users have been added
   */
  TeamDto addUsersToTeam(String teamName, List<UserTeamDto> userTeamDtoList);
}
