package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM;
import static java.util.Arrays.stream;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating team data.
 */
@Component
public class TeamValidator extends BaseValidator<TeamDto> {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  /**
   * Constructor for creating a TeamValidator instance.
   *
   * @param validator The validator object used to validate the team.
   * @param userRepository The repository responsible for accessing user data.
   * @param teamRepository The repository responsible for accessing team data.
   */
  public TeamValidator(Validator validator, UserRepository userRepository, TeamRepository teamRepository) {
    super(validator);
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
  }

  /**
   * Validates a TeamDto object.
   *
   * @param teamDto The TeamDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateTeamDto(TeamDto teamDto, Team... existingTeam) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(teamDto, errorMessages);
    validateTeamNameUniqueness(teamDto, errorMessages, existingTeam);
    validateUserHasTeamLeadershipRole(teamDto.creator(), errorMessages, existingTeam);
    throwIfErrorsExist(errorMessages);
  }

  private void validateTeamNameUniqueness(TeamDto teamDto, Set<String> errorMessages, Team... existingTeams) {
    String existingName = stream(existingTeams).findFirst().map(Team::getName).orElse(null);
    if (!teamDto.name().equals(existingName) && teamRepository.existsByName(teamDto.name())) {
      errorMessages.add(TEAM_ALREADY_EXISTS + teamDto.name());
    }
  }

  private void validateUserHasTeamLeadershipRole(UserDto userDto, Set<String> errorMessages, Team... existingTeams) {
    String existingName = existingTeams.length > 0 ? existingTeams[0].getName() : null;
    boolean isCreator = userRepository.isTeamCreator(existingName, userDto.username());
    boolean isUserInLeadershipPosition = userRepository.isUserInLeadershipPosition(userDto.username());
    boolean isUserInLeadershipPositionInTeam = userRepository.isUserInLeadershipPositionInTeam(existingName, userDto.username());
    if ((existingName == null && !isUserInLeadershipPosition) || (existingName != null && !isCreator && !isUserInLeadershipPositionInTeam)) {
      errorMessages.add(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM + userDto.username());
    }
  }
}
 
