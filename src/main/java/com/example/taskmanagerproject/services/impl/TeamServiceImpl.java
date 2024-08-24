package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.services.TeamService;
import com.example.taskmanagerproject.utils.factories.TeamFactory;
import com.example.taskmanagerproject.utils.factories.TeamUserFactory;
import com.example.taskmanagerproject.utils.mappers.TeamMapper;
import com.example.taskmanagerproject.utils.mappers.TeamUserMapper;
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

  private final TeamUserMapper teamUserMapper;
  private final TeamUserFactory teamUserFactory;
  private final TeamUserRepository teamUserRepository;

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
  public List<TeamDto> getTeamsBySlug(String slug) {
    List<Team> teamDtoList = teamRepository.findByUserSlug(slug);
    return teamDtoList.stream().map(teamMapper::toDto).toList();
  }

  @Override
  public List<TeamUserDto> getUsersWithRolesForTeam(String teamName) {
    return teamUserRepository.findAllByTeamName(teamName).stream().map(teamUserMapper::toDto).toList();
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
  @Transactional
  public List<TeamUserDto> addUsersToTeam(String teamName, List<TeamUserDto> teamUserDtoList) {
    List<TeamUser> teamUserList = teamUserFactory.createUserTeamAssociations(teamUserDtoList);
    teamUserRepository.saveAll(teamUserList);
    return teamUserList.stream().map(teamUserMapper::toDto).toList();
  }
}
