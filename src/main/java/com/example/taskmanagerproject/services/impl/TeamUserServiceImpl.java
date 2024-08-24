package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.services.TeamUserService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TeamUserService interface.
 */
@Service
@RequiredArgsConstructor
public class TeamUserServiceImpl implements TeamUserService {

  private final TeamUserRepository teamUserRepository;

  @Override
  public List<TeamUser> getAllByTeamName(String teamName) {
    return teamUserRepository.findAllByTeamName(teamName);
  }

  @Override
  public Role getRoleByTeamNameAndUsername(String teamName, String username) {
    return teamUserRepository.findRoleByTeamNameAndUsername(teamName, username);
  }

  @Override
  public Optional<Long> getRandomHigherRoleUser(Long teamId, Long userId) {
    return teamUserRepository.findRandomHigherRoleUser(teamId, userId);
  }

  @Override
  public boolean existsByUserIdAndTeamId(Long userId, Long teamId) {
    return teamUserRepository.existsByUserIdAndTeamId(userId, teamId);
  }
}
