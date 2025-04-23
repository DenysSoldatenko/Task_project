package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.roles.RoleHierarchy;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for the {@link TeamUserRepository} interface.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Retrieving all team-user associations by team name, including empty and non-existent team names</li>
 *   <li>Finding a role by team name and username, handling valid, invalid, and empty inputs</li>
 *   <li>Finding a random user with a higher role within a team, testing presence and absence scenarios</li>
 *   <li>Checking existence of a team-user association by user ID and team ID</li>
 * </ul>
 * </p>
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class TeamUserRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TeamUserRepository teamUserRepository;

  private Long userId;
  private String username;

  private User secondUser;
  private Long secondUserId;

  private Team team;
  private Long teamId;
  private String teamName;

  private Role adminRole;
  private Role memberRole;

  @BeforeEach
  void setUp() {
    User user = createUser();
    userId = user.getId();
    username = user.getUsername();

    secondUser = createUser();
    secondUserId = secondUser.getId();

    memberRole = createRole("MEMBER");
    adminRole = createRole("ADMIN123");

    team = createTeam(user);
    teamId = team.getId();
    teamName = team.getName();

    TeamUser teamUser = createTeamUser(user, team, memberRole);
    entityManager.persist(teamUser);

    RoleHierarchy roleHierarchy = createRoleHierarchy(adminRole, memberRole);
    entityManager.persist(roleHierarchy);

    entityManager.flush();
  }

  @Test
  public void findAllByTeamName_shouldReturnTeamUsers() {
    List<TeamUser> res = teamUserRepository.findAllByTeamName(teamName);
    assertEquals(1, res.size());
    assertEquals(userId, res.get(0).getUser().getId());
    assertEquals(teamId, res.get(0).getTeam().getId());
    assertEquals(memberRole.getName(), res.get(0).getRole().getName());
  }

  @Test
  public void findAllByTeamName_shouldReturnEmpty() {
    List<TeamUser> res = teamUserRepository.findAllByTeamName("NonExistent");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByTeamName_shouldHandleEmptyTeamName() {
    List<TeamUser> res = teamUserRepository.findAllByTeamName("");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByTeamName_shouldReturnMultipleUsers() {
    TeamUser secondTeamUser = createTeamUser(secondUser, team, memberRole);
    entityManager.persist(secondTeamUser);
    entityManager.flush();

    List<TeamUser> res = teamUserRepository.findAllByTeamName(teamName);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(tu -> tu.getUser().getId().equals(userId)));
    assertTrue(res.stream().anyMatch(tu -> tu.getUser().getId().equals(secondUserId)));
  }

  @Test
  public void findRoleByTeamNameAndUsername_shouldReturnRole() {
    Role res = teamUserRepository.findRoleByTeamNameAndUsername(teamName, username);
    assertNotNull(res);
    assertEquals(memberRole.getName(), res.getName());
  }

  @Test
  public void findRoleByTeamNameAndUsername_shouldReturnNullForNonExistentUsername() {
    Role res = teamUserRepository.findRoleByTeamNameAndUsername(teamName, "nonexistent@gmail.com");
    assertNull(res);
  }

  @Test
  public void findRoleByTeamNameAndUsername_shouldReturnNullForNonExistentTeam() {
    Role res = teamUserRepository.findRoleByTeamNameAndUsername("NonExistent", username);
    assertNull(res);
  }

  @Test
  public void findRoleByTeamNameAndUsername_shouldHandleEmptyInputs() {
    Role res = teamUserRepository.findRoleByTeamNameAndUsername("", "");
    assertNull(res);
  }

  @Test
  public void findRandomHigherRoleUser_shouldReturnUserId() {
    TeamUser adminTeamUser = createTeamUser(secondUser, team, adminRole);
    entityManager.persist(adminTeamUser);
    entityManager.flush();

    Optional<Long> res = teamUserRepository.findRandomHigherRoleUser(teamId, userId);
    assertTrue(res.isPresent());
    assertEquals(secondUserId, res.get());
  }

  @Test
  public void findRandomHigherRoleUser_shouldReturnEmptyForNoHigherRole() {
    Optional<Long> res = teamUserRepository.findRandomHigherRoleUser(teamId, userId);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findRandomHigherRoleUser_shouldReturnEmptyForNonExistentTeam() {
    Optional<Long> res = teamUserRepository.findRandomHigherRoleUser(999L, userId);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findRandomHigherRoleUser_shouldReturnEmptyForNonExistentUser() {
    TeamUser adminTeamUser = createTeamUser(secondUser, team, adminRole);
    entityManager.persist(adminTeamUser);
    entityManager.flush();

    Optional<Long> res = teamUserRepository.findRandomHigherRoleUser(teamId, 999L);
    assertTrue(res.isEmpty());
  }

  @Test
  public void existsByUserIdAndTeamId_shouldReturnTrue() {
    boolean exists = teamUserRepository.existsByUserIdAndTeamId(userId, teamId);
    assertTrue(exists);
  }

  @Test
  public void existsByUserIdAndTeamId_shouldReturnFalseForNonExistentUser() {
    boolean exists = teamUserRepository.existsByUserIdAndTeamId(999L, teamId);
    assertFalse(exists);
  }

  @Test
  public void existsByUserIdAndTeamId_shouldReturnFalseForNonExistentTeam() {
    boolean exists = teamUserRepository.existsByUserIdAndTeamId(userId, 999L);
    assertFalse(exists);
  }

  private User createUser() {
    User u = new User();
    u.setUsername("testuser" + System.nanoTime() + "@gmail.com");
    u.setFullName("Test User");
    u.setSlug("slug-" + System.nanoTime());
    entityManager.persist(u);
    return u;
  }

  private Team createTeam(User creator) {
    Team t = new Team();
    t.setName("My Team Name" + System.nanoTime());
    t.setDescription("Some description");
    t.setCreator(creator);
    t.setCreatedAt(now());
    entityManager.persist(t);
    return t;
  }

  private Role createRole(String name) {
    Role r = new Role();
    r.setName(name);
    entityManager.persist(r);
    return r;
  }

  private TeamUser createTeamUser(User user, Team team, Role role) {
    TeamUser tu = new TeamUser();
    tu.setId(new TeamUserId(user.getId(), team.getId()));
    tu.setUser(user);
    tu.setTeam(team);
    tu.setRole(role);
    return tu;
  }

  private RoleHierarchy createRoleHierarchy(Role higherRole, Role lowerRole) {
    RoleHierarchy rh = new RoleHierarchy();
    rh.setHigherRole(higherRole);
    rh.setLowerRole(lowerRole);
    return rh;
  }
}