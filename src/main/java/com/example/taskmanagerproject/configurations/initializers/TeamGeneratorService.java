package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static java.time.LocalDateTime.now;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
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

  private Team createTeam(User user) {
    Team team = new Team();
    team.setName(faker.team().name());
    team.setDescription(faker.lorem().sentence(10));
    team.setCreatedAt(now());
    team.setCreator(user);
    return team;
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

  /**
   * Creates a new TeamUser with an ADMIN role for the specified user and team.
   *
   * @param user The user to associate with the team.
   * @param team The team to associate with the user.
   * @throws ResourceNotFoundException if no ADMIN role is found in the system.
   */
  public void addAdminToTeam(User user, Team team) {
    TeamUser teamAdminUser = new TeamUser();
    teamAdminUser.setId(new TeamUserId(user.getId(), team.getId()));
    teamAdminUser.setUser(user);
    teamAdminUser.setTeam(team);
    teamAdminUser.setRole(getAdminRole());
    teamUserRepository.save(teamAdminUser);
  }

  private TeamUser createTeamUser(User user, Team team) {
    TeamUser teamUser = new TeamUser();
    teamUser.setId(new TeamUserId(user.getId(), team.getId()));
    teamUser.setUser(user);
    teamUser.setTeam(team);
    teamUser.setRole(getRandomRole());
    return teamUser;
  }

  private Role getAdminRole() {
    return roleRepository.findAll().stream()
      .filter(role -> "ADMIN".equals(role.getName()))
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + "ADMIN"));
  }

  private Role getRandomRole() {
    return roleRepository.findAll().get(faker.number().numberBetween(1, roleRepository.findAll().size() - 1));
  }
}
