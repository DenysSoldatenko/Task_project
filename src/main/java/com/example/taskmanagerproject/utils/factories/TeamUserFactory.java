package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_ALREADY_IN_TEAM;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for handling User-Team associations and constructing UserTeam entities.
 */
@Component
@RequiredArgsConstructor
public final class TeamUserFactory {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final RoleRepository roleRepository;
  private final TeamUserRepository teamUserRepository;

  /**
   * Creates a list of UserTeam entities from the provided list of UserTeamDto objects.
   *
   * @param userTeams The list of UserTeamDto objects to create UserTeam entities.
   * @return A list of {@link TeamUser} entities representing the user-team assignments.
   */
  public List<TeamUser> createUserTeamAssociations(List<TeamUserDto> userTeams) {
    List<TeamUser> teamUserAssociations = new ArrayList<>();

    for (TeamUserDto teamUserDto : userTeams) {
      User user = getUserFromRequest(teamUserDto);
      Team team = getTeamFromRequest(teamUserDto);
      Role role = getRoleFromRequest(teamUserDto);
      checkUserAlreadyInTeam(user, team);
      teamUserAssociations.add(buildUserTeam(user, team, role));
    }

    return teamUserAssociations;
  }

  /**
   * Retrieves the User entity based on the user from the UserTeamDto.
   *
   * @param teamUserDto The UserTeamDto containing the user.
   * @return The {@link User} entity corresponding to the user.
   * @throws ResourceNotFoundException if the user with the specified user is not found.
   */
  private User getUserFromRequest(TeamUserDto teamUserDto) {
    return userRepository.findByUsername(teamUserDto.user().username())
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_USERNAME + teamUserDto.user().username()));
  }

  /**
   * Retrieves the Team entity based on the team from the UserTeamDto.
   *
   * @param teamUserDto The UserTeamDto containing the team.
   * @return The {@link Team} entity corresponding to the team.
   * @throws ResourceNotFoundException if the team with the specified team is not found.
   */
  private Team getTeamFromRequest(TeamUserDto teamUserDto) {
    return teamRepository.findByName(teamUserDto.team().name())
        .orElseThrow(() -> new ResourceNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamUserDto.team().name()));
  }

  /**
   * Retrieves the Role entity based on the role from the UserTeamDto.
   *
   * @param teamUserDto The UserTeamDto containing the role.
   * @throws ResourceNotFoundException if the role with the specified role is not found.
   */
  private Role getRoleFromRequest(TeamUserDto teamUserDto) {
    return roleRepository.findByName(teamUserDto.role().name())
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + teamUserDto.role().name()));
  }

  private void checkUserAlreadyInTeam(User user, Team team) {
    if (teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())) {
      throw new ValidationException(format(USER_ALREADY_IN_TEAM, user.getId(), team.getId()));
    }
  }

  private TeamUser buildUserTeam(User user, Team team, Role role) {
    TeamUser teamUser = new TeamUser();
    teamUser.setId(new TeamUserId(user.getId(), team.getId()));
    teamUser.setUser(user);
    teamUser.setTeam(team);
    teamUser.setRole(role);
    return teamUser;
  }
}
