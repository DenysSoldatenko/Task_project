package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.entities.Team;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.utils.factories.TeamFactory;
import com.example.taskmanagerproject.utils.mappers.TeamMapper;
import com.example.taskmanagerproject.utils.validators.TeamValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the TeamService interface.
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

  private final TeamMapper teamMapper;
  private final TeamFactory teamFactory;
  private final TeamValidator teamValidator;
  private final TeamRepository teamRepository;

  /**
   * Creates a new team.
   *
   * @param teamDto the data transfer object containing team details
   * @return the created team data
   */
  @Override
  @Transactional
  public TeamDto createTeam(TeamDto teamDto) {
    teamValidator.validateTeamDto(teamDto);
    Team newTeam = teamFactory.createProjectFromRequest(teamDto);
    teamRepository.save(newTeam);
    return teamMapper.toDto(newTeam);
  }

  /**
   * Retrieves a team by its name.
   *
   * @param teamName the name of the team
   * @return the team data if found
   * @throws TeamNotFoundException if the team with the given name does not exist
   */
  @Override
  public TeamDto getTeamByName(String teamName) {
    Team team = teamRepository.findByName(teamName)
        .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));
    return teamMapper.toDto(team);
  }

  /**
   * Updates an existing team.
   *
   * @param teamName the name of the team to update
   * @param teamDto  the data transfer object containing updated team details
   * @return the updated team data
   * @throws TeamNotFoundException if the team with the given name does not exist
   */
  @Override
  @Transactional
  public TeamDto updateTeam(String teamName, TeamDto teamDto) {
    Team existingTeam = teamRepository.findByName(teamName)
        .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));

    teamValidator.validateTeamDto(teamDto, existingTeam);
    existingTeam.setName(teamDto.name());
    existingTeam.setDescription(teamDto.description());

    teamRepository.save(existingTeam);

    return teamMapper.toDto(existingTeam);
  }

  /**
   * Deletes a team by its name.
   *
   * @param teamName the name of the team to delete
   * @throws TeamNotFoundException if the team with the given name does not exist
   */
  @Override
  @Transactional
  public void deleteTeam(String teamName) {
    Team existingTeam = teamRepository.findByName(teamName)
        .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));
    teamRepository.delete(existingTeam);
  }

  @Override
  public List<TeamDto> getTeamsBySlug(String slug) {
    List<Team> teamDtoList = teamRepository.findByCreatorSlug(slug);
    return teamDtoList.stream().map(teamMapper::toDto).toList();
  }
}
