package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class TeamRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TeamRepository teamRepository;

  private User user;
  private Long userId;
  private String userSlug;

  private User secondUser;
  private String secondUserSlug;

  private Role role;

  private String teamName;

  @BeforeEach
  void setUp() {
    user = createUser();
    userId = user.getId();
    userSlug = user.getSlug();

    secondUser = createUser();
    secondUserSlug = secondUser.getSlug();

    role = createRole();

    Team team = createTeam(user);
    teamName = team.getName();

    TeamUser teamUser = createTeamUser(secondUser, team, role);
    entityManager.persist(teamUser);
    entityManager.flush();
  }

  @Test
  public void findByName_shouldReturnEmpty() {
    assertTrue(teamRepository.findByName("NonExistent").isEmpty());
  }

  @Test
  public void findByName_shouldHandleEmptyName() {
    assertTrue(teamRepository.findByName("").isEmpty());
  }

  @Test
  public void existsByName_shouldReturnTrue() {
    boolean exists = teamRepository.existsByName(teamName);
    assertTrue(exists);
  }

  @Test
  public void existsByName_shouldReturnFalse() {
    boolean exists = teamRepository.existsByName("NonExistent");
    assertFalse(exists);
  }

  @Test
  public void existsByName_shouldHandleEmptyName() {
    boolean exists = teamRepository.existsByName("");
    assertFalse(exists);
  }

  @Test
  public void findByUserSlug_shouldReturnTeamsForCreator() {
    List<Team> res = teamRepository.findByUserSlug(userSlug);
    assertEquals(1, res.size());
    assertEquals(teamName, res.get(0).getName());
    assertEquals(userId, res.get(0).getCreator().getId());
  }

  @Test
  public void findByUserSlug_shouldReturnEmpty() {
    List<Team> res = teamRepository.findByUserSlug("nonexistent-slug");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByUserSlug_shouldHandleEmptySlug() {
    List<Team> res = teamRepository.findByUserSlug("");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByUserSlug_shouldReturnMultipleTeams() {
    Team secondTeam = createTeam(user);
    TeamUser secondTeamUser = createTeamUser(secondUser, secondTeam, role);
    entityManager.persist(secondTeamUser);
    entityManager.flush();

    List<Team> res = teamRepository.findByUserSlug(userSlug);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(t -> t.getName().equals(teamName)));
    assertTrue(res.stream().anyMatch(t -> t.getName().equals(secondTeam.getName())));

    List<Team> resMember = teamRepository.findByUserSlug(secondUserSlug);
    assertEquals(2, resMember.size());
    assertTrue(resMember.stream().anyMatch(t -> t.getName().equals(teamName)));
    assertTrue(resMember.stream().anyMatch(t -> t.getName().equals(secondTeam.getName())));
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

  private Role createRole() {
    Role r = new Role();
    r.setName("MEMBER");
    entityManager.persist(r);
    return r;
  }

  private TeamUser createTeamUser(User user, Team team, Role role) {
    TeamUser tu = new TeamUser();
    tu.setUser(user);
    tu.setTeam(team);
    tu.setRole(role);
    return tu;
  }
}