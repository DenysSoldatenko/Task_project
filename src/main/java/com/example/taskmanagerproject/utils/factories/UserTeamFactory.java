package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_ALREADY_IN_TEAM;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_ID;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.UserTeamDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.Team;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.entities.UserTeam;
import com.example.taskmanagerproject.entities.UserTeamId;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Factory for handling User-Team associations and constructing UserTeam entities.
 */
@Component
@RequiredArgsConstructor
public class UserTeamFactory {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final RoleRepository roleRepository;

  /**
   * Creates a list of UserTeam entities from the provided list of UserTeamDto objects.
   *
   * @param userTeams The list of UserTeamDto objects to create UserTeam entities.
   * @return A list of {@link UserTeam} entities representing the user-team assignments.
   */
  public List<UserTeam> createUserTeamAssociations(final List<UserTeamDto> userTeams) {
    List<UserTeam> userTeamAssociations = new ArrayList<>();

    for (UserTeamDto userTeamDto : userTeams) {
      User user = getUserFromRequest(userTeamDto);
      Team team = getTeamFromRequest(userTeamDto);
      Role role = getRoleFromRequest(userTeamDto);
      checkUserAlreadyInTeam(user, team);
      userTeamAssociations.add(buildUserTeam(user, team, role));
    }

    return userTeamAssociations;
  }

  /**
   * Retrieves the User entity based on the userId from the UserTeamDto.
   *
   * @param userTeamDto The UserTeamDto containing the userId.
   * @return The {@link User} entity corresponding to the userId.
   * @throws UsernameNotFoundException if the user with the specified userId is not found.
   */
  private User getUserFromRequest(final UserTeamDto userTeamDto) {
    return userRepository.findById(userTeamDto.userId())
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_ID + userTeamDto.userId()));
  }

  /**
   * Retrieves the Team entity based on the teamId from the UserTeamDto.
   *
   * @param userTeamDto The UserTeamDto containing the teamId.
   * @return The {@link Team} entity corresponding to the teamId.
   * @throws TeamNotFoundException if the team with the specified teamId is not found.
   */
  private Team getTeamFromRequest(final UserTeamDto userTeamDto) {
    return teamRepository.findById(userTeamDto.teamId())
        .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_ID + userTeamDto.teamId()));
  }

  /**
   * Retrieves the Role entity based on the roleId from the UserTeamDto.
   *
   * @param userTeamDto The UserTeamDto containing the roleId.
   * @throws RoleNotFoundException if the role with the specified roleId is not found.
   */
  private Role getRoleFromRequest(final UserTeamDto userTeamDto) {
    return roleRepository.findById(userTeamDto.roleId())
        .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_ID + userTeamDto.roleId()));
  }

  private void checkUserAlreadyInTeam(final User user, final Team team) {
    if (teamRepository.existsByUserIdAndTeamId(user.getId(), team.getId())) {
      throw new ValidationException(format(USER_ALREADY_IN_TEAM, user.getId(), team.getId()));
    }
  }

  private UserTeam buildUserTeam(final User user, final Team team, final Role role) {
    UserTeam userTeam = new UserTeam();
    userTeam.setId(new UserTeamId(user.getId(), team.getId()));
    userTeam.setUser(user);
    userTeam.setTeam(team);
    userTeam.setRole(role);
    return userTeam;
  }
}
