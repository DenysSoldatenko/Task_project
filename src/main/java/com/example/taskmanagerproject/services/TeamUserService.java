package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.entities.security.Role;
import com.example.taskmanagerproject.entities.team.TeamUser;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing TeamUser entities.
 */
public interface TeamUserService {

  /**
   * Retrieves a list of all TeamUser entities by the name of the team.
   *
   * @param teamName the name of the team to filter by.
   * @return a list of TeamUser entities associated with the specified team.
   */
  List<TeamUser> getAllByTeamName(String teamName);

  /**
   * Retrieves the role of a user in a specific team.
   *
   * @param teamName the name of the team.
   * @param username the username of the user.
   * @return the role of the user in the specified team.
   */
  Role getRoleByTeamNameAndUsername(String teamName, String username);

  /**
   * Finds a random user within the same team who has a higher role than the specified user.
   * The role hierarchy is checked to ensure the user found has a role that is considered higher.
   *
   * @param teamId the ID of the team the user belongs to.
   * @param userId the ID of the user whose role will be used to filter the other users.
   * @return an Optional containing the ID of a random user with a higher role, or an empty Optional if no such user exists.
   */
  Optional<Long> getRandomHigherRoleUser(Long teamId, Long userId);

  /**
   * Checks if a UserTeam exists based on user and team using a native query.
   *
   * @param userId The user's ID.
   * @param teamId The team's ID.
   * @return True if a UserTeam exists, otherwise False.
   */
  boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
