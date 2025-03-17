package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import java.util.Optional;
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
public class RoleRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RoleRepository roleRepository;

  private Team team;
  private Long userId;
  private Long teamId;
  private String roleName;

  @BeforeEach
  void setUp() {
    User user = createUser();
    userId = user.getId();

    team = createTeam(user);
    teamId = team.getId();

    Role role = createRole("ADMIN123");
    roleName = role.getName();

    TeamUser teamUser = createTeamUser(user, team, role);
    entityManager.persist(teamUser);
    entityManager.flush();
  }

  @Test
  public void findByName_shouldReturnRole() {
    Optional<Role> res = roleRepository.findByName(roleName);
    assertTrue(res.isPresent());
    assertEquals(roleName, res.get().getName());
  }

  @Test
  public void findByName_shouldReturnEmpty() {
    assertTrue(roleRepository.findByName("NONEXISTENT").isEmpty());
  }

  @Test
  public void findByName_shouldHandleEmptyName() {
    assertTrue(roleRepository.findByName("").isEmpty());
  }

  @Test
  public void existsByName_shouldReturnTrue() {
    boolean exists = roleRepository.existsByName(roleName);
    assertTrue(exists);
  }

  @Test
  public void existsByName_shouldReturnFalse() {
    boolean exists = roleRepository.existsByName("NONEXISTENT");
    assertFalse(exists);
  }

  @Test
  public void existsByName_shouldHandleEmptyName() {
    boolean exists = roleRepository.existsByName("");
    assertFalse(exists);
  }

  @Test
  public void getRoleForUserInTeam_shouldReturnRole() {
    Role res = roleRepository.getRoleForUserInTeam(userId, teamId);
    assertNotNull(res);
    assertEquals(roleName, res.getName());
  }

  @Test
  public void getRoleForUserInTeam_shouldReturnNullForNonExistentUser() {
    Role res = roleRepository.getRoleForUserInTeam(999L, teamId);
    assertNull(res);
  }

  @Test
  public void getRoleForUserInTeam_shouldReturnNullForNonExistentTeam() {
    Role res = roleRepository.getRoleForUserInTeam(userId, 999L);
    assertNull(res);
  }

  @Test
  public void getRoleForUserInTeam_shouldHandleMultipleUsersInTeam() {
    User secondUser = createUser();
    Role memberRole = createRole("MEMBER");
    TeamUser secondTeamUser = createTeamUser(secondUser, team, memberRole);
    entityManager.persist(secondTeamUser);
    entityManager.flush();

    Role res = roleRepository.getRoleForUserInTeam(secondUser.getId(), teamId);
    assertNotNull(res);
    assertEquals("MEMBER", res.getName());
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
    tu.setUser(user);
    tu.setTeam(team);
    tu.setRole(role);
    return tu;
  }
}