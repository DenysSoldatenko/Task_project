package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.projects.ProjectTeamId;
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
public class ProjectRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ProjectRepository projectRepository;

  private Long userId;
  private Long teamId;
  private Long projectId;
  private String projectName, username, slug, secondUsername;
  private User user, secondUser;
  private Team team;
  private Role role;
  private Project project;

  @BeforeEach
  void setUp() {
    user = createUser();
    userId = user.getId();
    username = user.getUsername();
    slug = user.getSlug();

    secondUser = createUser();
    secondUsername = secondUser.getUsername();

    team = createTeam(user);
    teamId = team.getId();

    project = createProject(user);
    projectId = project.getId();
    projectName = project.getName();

    role = createRole();

    TeamUser teamUser = createTeamUser(user, team, role);
    ProjectTeam projectTeam = createProjectTeam(project, team);

    entityManager.persist(teamUser);
    entityManager.persist(projectTeam);
    entityManager.flush();
  }

  @Test
  public void findByName_shouldReturnEmpty() {
    assertTrue(projectRepository.findByName("NonExistent").isEmpty());
  }

  @Test
  public void findByName_shouldHandleEmptyName() {
    assertTrue(projectRepository.findByName("").isEmpty());
  }

  @Test
  public void findRoleByProjectNameAndUsername_shouldReturnRole() {
    Role res = projectRepository.findRoleByProjectNameAndUsername(projectName, username);
    assertEquals(role, res);
  }

  @Test
  public void findRoleByProjectNameAndUsername_shouldReturnNullForNonExistentUsername() {
    Role res = projectRepository.findRoleByProjectNameAndUsername(projectName, "nonexistent@gmail.com");
    assertNull(res);
  }

  @Test
  public void findRoleByProjectNameAndUsername_shouldReturnNullForNonExistentProject() {
    Role res = projectRepository.findRoleByProjectNameAndUsername("NonExistent", username);
    assertNull(res);
  }

  @Test
  public void findRoleByProjectNameAndUsername_shouldHandleEmptyInputs() {
    Role res = projectRepository.findRoleByProjectNameAndUsername("", "");
    assertNull(res);
  }

  @Test
  public void findRoleByProjectNameAndUsername_shouldReturnRoleForMultipleUsers() {
    TeamUser secondTeamUser = createTeamUser(secondUser, team, role);
    entityManager.persist(secondTeamUser);
    entityManager.flush();

    Role res = projectRepository.findRoleByProjectNameAndUsername(projectName, secondUsername);
    assertEquals(role, res);
  }

  @Test
  public void existsByUserIdAndProjectId_shouldReturnTrue() {
    boolean exists = projectRepository.existsByUserIdAndProjectId(userId, projectId);
    assertTrue(exists);
  }

  @Test
  public void existsByUserIdAndProjectId_shouldReturnFalseForNonExistentUser() {
    boolean exists = projectRepository.existsByUserIdAndProjectId(999L, projectId);
    assertFalse(exists);
  }

  @Test
  public void existsByUserIdAndProjectId_shouldReturnFalseForNonExistentProject() {
    boolean exists = projectRepository.existsByUserIdAndProjectId(userId, 999L);
    assertFalse(exists);
  }

  @Test
  public void existsByName_shouldReturnTrue() {
    boolean exists = projectRepository.existsByName(projectName);
    assertTrue(exists);
  }

  @Test
  public void existsByName_shouldReturnFalse() {
    boolean exists = projectRepository.existsByName("NonExistent");
    assertFalse(exists);
  }

  @Test
  public void existsByName_shouldReturnFalseForEmptyName() {
    boolean exists = projectRepository.existsByName("");
    assertFalse(exists);
  }

  @Test
  public void findByUserSlug_shouldReturnProjects() {
    List<Project> res = projectRepository.findByUserSlug(slug);
    assertEquals(1, res.size());
    assertEquals(projectName, res.get(0).getName());
  }

  @Test
  public void findByUserSlug_shouldReturnEmpty() {
    List<Project> res = projectRepository.findByUserSlug("nonexistent-slug");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByUserSlug_shouldReturnMultipleProjects() {
    Project secondProject = createProject(user);
    ProjectTeam secondProjectTeam = createProjectTeam(secondProject, team);
    entityManager.persist(secondProjectTeam);
    entityManager.flush();

    List<Project> res = projectRepository.findByUserSlug(slug);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(p -> p.getName().equals(projectName)));
    assertTrue(res.stream().anyMatch(p -> p.getName().equals(secondProject.getName())));
  }

  @Test
  public void findByUserSlug_shouldHandleEmptySlug() {
    List<Project> res = projectRepository.findByUserSlug("");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByUserSlug_shouldReturnProjectsForMultipleTeams() {
    Team secondTeam = createTeam(user);
    TeamUser secondTeamUser = createTeamUser(user, secondTeam, role);
    ProjectTeam secondProjectTeam = createProjectTeam(project, secondTeam);
    entityManager.persist(secondTeamUser);
    entityManager.persist(secondProjectTeam);
    entityManager.flush();

    List<Project> res = projectRepository.findByUserSlug(slug);
    assertEquals(1, res.size()); // Project appears once despite multiple teams
    assertEquals(projectName, res.get(0).getName());
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
    t.setName("My Team Name " + System.nanoTime());
    t.setDescription("Some description");
    t.setCreator(creator);
    t.setCreatedAt(now());
    entityManager.persist(t);
    return t;
  }

  private Role createRole() {
    Role role = new Role();
    role.setName("ROLE_MEMBER_" + System.nanoTime());
    role.setDescription("Test role description");
    entityManager.persist(role);
    return role;
  }

  private Project createProject(User creator) {
    Project p = new Project();
    p.setName("My Project Name" + System.nanoTime());
    p.setDescription("Some description");
    p.setCreator(creator);
    p.setCreatedAt(now());
    entityManager.persist(p);
    return p;
  }

  private TeamUser createTeamUser(User user, Team team, Role role) {
    TeamUser tu = new TeamUser();
    tu.setUser(user);
    tu.setTeam(team);
    tu.setRole(role);
    return tu;
  }

  private ProjectTeam createProjectTeam(Project project, Team team) {
    ProjectTeam pt = new ProjectTeam();
    pt.setId(new ProjectTeamId(team.getId(), project.getId()));
    pt.setProject(project);
    pt.setTeam(team);

    return pt;
  }

}