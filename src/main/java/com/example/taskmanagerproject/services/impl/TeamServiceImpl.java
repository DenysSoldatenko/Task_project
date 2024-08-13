package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.dtos.UserTeamDto;
import com.example.taskmanagerproject.entities.Team;
import com.example.taskmanagerproject.entities.UserTeam;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserTeamRepository;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.utils.factories.TeamFactory;
import com.example.taskmanagerproject.utils.factories.UserTeamFactory;
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
  private final UserTeamFactory userTeamFactory;
  private final TeamRepository teamRepository;
  private final UserTeamRepository userTeamRepository;

  @Override
  @Transactional
  public TeamDto createTeam(TeamDto teamDto) {
    teamValidator.validateTeamDto(teamDto);
    Team newTeam = teamFactory.createProjectFromRequest(teamDto);
    teamRepository.save(newTeam);
    return teamMapper.toDto(newTeam);
  }

  @Override
  public TeamDto getTeamByName(String teamName) {
    Team team = teamRepository.findByName(teamName)
        .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));
    return teamMapper.toDto(team);
  }

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

  @Override
  @Transactional
  public TeamDto addUsersToTeam(String teamName, List<UserTeamDto> userTeamDtoList) {
    List<UserTeam> userTeamList = userTeamFactory.createUserTeamAssociations(userTeamDtoList);
    userTeamRepository.saveAll(userTeamList);
    return getTeamByName(teamName);
  }
}
