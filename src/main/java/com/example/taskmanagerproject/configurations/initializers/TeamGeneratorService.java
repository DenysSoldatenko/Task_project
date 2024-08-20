package com.example.taskmanagerproject.configurations.initializers;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.security.Role;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.entities.team.TeamUser;
import com.example.taskmanagerproject.entities.team.TeamUserId;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

/**
 * Service for generating team-related data.
 */
@Service
@RequiredArgsConstructor
public class TeamGeneratorService {

  private final Faker faker = new Faker();
  private final RoleRepository roleRepository;
  private final TeamRepository teamRepository;
  private final TeamUserRepository teamUserRepository;

  /**
   * Generates a batch of teams for a specific user.
   *
   * @param user      The creator of the teams.
   * @param batchSize The number of teams to generate.
   * @return A list of generated teams.
   */
  public List<Team> generateTeams(User user, int batchSize) {
    return teamRepository.saveAll(range(0, batchSize).mapToObj(i -> createTeam(user)).toList());
  }

  /**
   * Generates a list of TeamUser associations for a given team and a list of users.
   *
   * @param users The list of users to be associated with the team.
   * @param team  The team with which the users will be associated.
   * @return A list of TeamUser associations between the provided users and the given team.
   */
  public List<TeamUser> generateTeamUsers(List<User> users, Team team) {
    return teamUserRepository.saveAll(users.stream().map(user -> createTeamUser(user, team)).toList());
  }

  private Team createTeam(User user) {
    Team team = new Team();
    team.setName(faker.team().name() + randomUUID().toString().substring(0, 4));
    team.setDescription(faker.lorem().sentence(10));
    team.setCreatedAt(now());
    team.setCreator(user);
    return team;
  }

  private TeamUser createTeamUser(User user, Team team) {
    TeamUser teamUser = new TeamUser();
    teamUser.setId(new TeamUserId(user.getId(), team.getId()));
    teamUser.setUser(user);
    teamUser.setTeam(team);
    teamUser.setRole(getRandomRole());
    return teamUser;
  }

  private Role getRandomRole() {
    return roleRepository.findAll().get(faker.number().numberBetween(0, roleRepository.findAll().size()));
  }
}
