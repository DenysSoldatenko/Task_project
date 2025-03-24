package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.projects.ProjectTeamId;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for the {@link UserRepository} interface.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Finding users by username and slug, including empty and non-existent values</li>
 *   <li>Checking if a user is the owner of a task or assigned to a task</li>
 *   <li>Verifying project and team creator status</li>
 *   <li>Determining if a user holds a leadership position in teams or projects</li>
 *   <li>General leadership role checks across the system</li>
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
public class UserRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  private Long taskId;

  private Long userId;
  private String username;

  private Long secondUserId;
  private String secondUsername;

  private Team team;
  private String teamName;

  private Project project;
  private String projectName;

  @BeforeEach
  void setUp() {
    User user = createUser();
    userId = user.getId();
    username = user.getUsername();

    User secondUser = createUser();
    secondUsername = secondUser.getUsername();
    secondUserId = secondUser.getId();

    team = createTeam(user);
    teamName = team.getName();

    project = createProject(user);
    projectName = project.getName();

    Task task = createTask(team, project, user, secondUser);
    taskId = task.getId();

    Role memberRole = createRole("MEMBER");
    Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> createRole("ADMIN123"));

    TeamUser teamUser = createTeamUser(user, team, adminRole);
    TeamUser secondTeamUser = createTeamUser(secondUser, team, memberRole);
    entityManager.persist(teamUser);
    entityManager.persist(secondTeamUser);

    entityManager.flush();
  }

  @Test
  public void findByUsername_shouldReturnEmpty() {
    assertTrue(userRepository.findByUsername("nonexistent@gmail.com").isEmpty());
  }

  @Test
  public void findByUsername_shouldHandleEmptyUsername() {
    assertTrue(userRepository.findByUsername("").isEmpty());
  }

  @Test
  public void findBySlug_shouldReturnEmpty() {
    assertTrue(userRepository.findBySlug("nonexistent-slug").isEmpty());
  }

  @Test
  public void findBySlug_shouldHandleEmptySlug() {
    assertTrue(userRepository.findBySlug("").isEmpty());
  }

  @Test
  public void isTaskOwner_shouldReturnTrue() {
    boolean res = userRepository.isTaskOwner(userId, taskId);
    assertTrue(res);
  }

  @Test
  public void isTaskOwner_shouldReturnFalseForNonExistentUser() {
    boolean res = userRepository.isTaskOwner(999L, taskId);
    assertFalse(res);
  }

  @Test
  public void isTaskOwner_shouldReturnFalseForNonExistentTask() {
    boolean res = userRepository.isTaskOwner(userId, 999L);
    assertFalse(res);
  }

  @Test
  public void isUserAssignedToTask_shouldReturnTrue() {
    boolean res = userRepository.isUserAssignedToTask(secondUserId, taskId);
    assertTrue(res);
  }

  @Test
  public void isUserAssignedToTask_shouldReturnFalseForNonExistentUser() {
    boolean res = userRepository.isUserAssignedToTask(999L, taskId);
    assertFalse(res);
  }

  @Test
  public void isUserAssignedToTask_shouldReturnFalseForNonExistentTask() {
    boolean res = userRepository.isUserAssignedToTask(secondUserId, 999L);
    assertFalse(res);
  }

  @Test
  public void isProjectCreator_shouldReturnTrue() {
    boolean res = userRepository.isProjectCreator(projectName, username);
    assertTrue(res);
  }

  @Test
  public void isProjectCreator_shouldReturnFalseForNonExistentProject() {
    boolean res = userRepository.isProjectCreator("NonExistent", username);
    assertFalse(res);
  }

  @Test
  public void isProjectCreator_shouldReturnFalseForNonExistentUsername() {
    boolean res = userRepository.isProjectCreator(projectName, "nonexistent@gmail.com");
    assertFalse(res);
  }

  @Test
  public void isProjectCreator_shouldHandleEmptyInputs() {
    boolean res = userRepository.isProjectCreator("", "");
    assertFalse(res);
  }

  @Test
  public void isTeamCreator_shouldReturnTrue() {
    boolean res = userRepository.isTeamCreator(teamName, username);
    assertTrue(res);
  }

  @Test
  public void isTeamCreator_shouldReturnFalseForNonExistentTeam() {
    boolean res = userRepository.isTeamCreator("NonExistent", username);
    assertFalse(res);
  }

  @Test
  public void isTeamCreator_shouldReturnFalseForNonExistentUsername() {
    boolean res = userRepository.isTeamCreator(teamName, "nonexistent@gmail.com");
    assertFalse(res);
  }

  @Test
  public void isTeamCreator_shouldHandleEmptyInputs() {
    boolean res = userRepository.isTeamCreator("", "");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInTeam_shouldReturnTrue() {
    boolean res = userRepository.isUserInLeadershipPositionInTeam(teamName, username);
    assertTrue(res);
  }

  @Test
  public void isUserInLeadershipPositionInTeam_shouldReturnFalseForNonLeadershipRole() {
    boolean res = userRepository.isUserInLeadershipPositionInTeam(teamName, secondUsername);
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInTeam_shouldReturnFalseForNonExistentTeam() {
    boolean res = userRepository.isUserInLeadershipPositionInTeam("NonExistent", username);
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInTeam_shouldReturnFalseForNonExistentUsername() {
    boolean res = userRepository.isUserInLeadershipPositionInTeam(teamName, "nonexistent@gmail.com");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInTeam_shouldHandleEmptyInputs() {
    boolean res = userRepository.isUserInLeadershipPositionInTeam("", "");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInProject_shouldReturnTrue() {
    entityManager.persist(createProjectTeam(project, team));
    entityManager.flush();

    boolean res = userRepository.isUserInLeadershipPositionInProject(projectName, username);
    assertTrue(res);
  }

  @Test
  public void isUserInLeadershipPositionInProject_shouldReturnFalseForNonLeadershipRole() {
    entityManager.persist(createProjectTeam(project, team));
    entityManager.flush();

    boolean res = userRepository.isUserInLeadershipPositionInProject(projectName, secondUsername);
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInProject_shouldReturnFalseForNonExistentProject() {
    boolean res = userRepository.isUserInLeadershipPositionInProject("NonExistent", username);
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInProject_shouldReturnFalseForNonExistentUsername() {
    entityManager.persist(createProjectTeam(project, team));
    entityManager.flush();

    boolean res = userRepository.isUserInLeadershipPositionInProject(projectName, "nonexistent@gmail.com");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPositionInProject_shouldHandleEmptyInputs() {
    boolean res = userRepository.isUserInLeadershipPositionInProject("", "");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPosition_shouldReturnTrue() {
    boolean res = userRepository.isUserInLeadershipPosition(username);
    assertTrue(res);
  }

  @Test
  public void isUserInLeadershipPosition_shouldReturnFalseForNonLeadershipRole() {
    boolean res = userRepository.isUserInLeadershipPosition(secondUsername);
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPosition_shouldReturnFalseForNonExistentUsername() {
    boolean res = userRepository.isUserInLeadershipPosition("nonexistent@gmail.com");
    assertFalse(res);
  }

  @Test
  public void isUserInLeadershipPosition_shouldHandleEmptyUsername() {
    boolean res = userRepository.isUserInLeadershipPosition("");
    assertFalse(res);
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

  private Project createProject(User creator) {
    Project p = new Project();
    p.setName("My Project Name" + System.nanoTime());
    p.setDescription("Some description");
    p.setCreator(creator);
    p.setCreatedAt(now());
    entityManager.persist(p);
    return p;
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

  private Task createTask(Team team, Project project, User sender, User receiver) {
    Task task = new Task();
    task.setTitle("Test Task " + System.nanoTime());
    task.setDescription("Test description");
    task.setCreatedAt(now());
    task.setPriority(TaskPriority.MEDIUM);
    task.setTeam(team);
    task.setProject(project);
    task.setAssignedBy(sender);
    task.setAssignedTo(receiver);
    entityManager.persist(task);
    return task;
  }

  private ProjectTeam createProjectTeam(Project project, Team team) {
    ProjectTeam pt = new ProjectTeam();
    pt.setId(new ProjectTeamId(project.getId(), team.getId()));
    pt.setProject(project);
    pt.setTeam(team);
    return pt;
  }
}