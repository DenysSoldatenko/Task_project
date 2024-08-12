package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_TEAM;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.entities.Team;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating team data.
 */
@Component
public class TeamValidator extends BaseValidator<TeamDto> {

  private final TeamRepository teamRepository;

  /**
   * Constructor for creating a TeamValidator instance.
   *
   * @param validator The validator object used to validate the team.
   * @param userRepository The repository responsible for accessing user data.
   * @param teamRepository The repository responsible for accessing team data.
   */
  public TeamValidator(
      Validator validator, UserRepository userRepository, TeamRepository teamRepository
  ) {
    super(validator, userRepository);
    this.teamRepository = teamRepository;
  }

  /**
   * Validates a TeamDto object.
   *
   * @param teamDto The TeamDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateTeamDto(final TeamDto teamDto, final Team... existingTeam) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(teamDto, errorMessages);
    validateNameTaken(
        teamDto.name(),
        Arrays.stream(existingTeam).findFirst().map(Team::getName).orElse(null),
        teamRepository.existsByName(teamDto.name()),
        errorMessages,
        TEAM_ALREADY_EXISTS
    );
    validateCreatorRole(
        teamDto.creator(),
        USER_DOES_NOT_HAVE_ROLE_TO_CREATE_TEAM,
        errorMessages
    );
    throwIfErrorsExist(errorMessages);
  }
}
 
