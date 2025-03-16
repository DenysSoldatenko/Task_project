package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.projects.ProjectTeamId;
import com.example.taskmanagerproject.entities.teams.Team;
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
public class ProjectTeamRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ProjectTeamRepository projectTeamRepository;

  private Long projectId;
  private Long teamId;
  private String projectName, teamName;
  private User user;
  private Project project;
  private Team team;

  @BeforeEach
  void setUp() {
    user = createUser();

    project = createProject(user);
    projectId = project.getId();
    projectName = project.getName();

    team = createTeam(user);
    teamId = team.getId();
    teamName = team.getName();

    ProjectTeam projectTeam = createProjectTeam(project, team);

    entityManager.persist(projectTeam);
    entityManager.flush();
  }

  @Test
  public void findAllByProjectName_shouldReturnProjectTeams() {
    List<ProjectTeam> res = projectTeamRepository.findAllByProjectName(projectName);
    assertEquals(1, res.size());
    assertEquals(projectId, res.get(0).getProject().getId());
    assertEquals(teamId, res.get(0).getTeam().getId());
  }

  @Test
  public void findAllByProjectName_shouldReturnEmpty() {
    List<ProjectTeam> res = projectTeamRepository.findAllByProjectName("NonExistent");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByProjectName_shouldHandleEmptyProjectName() {
    List<ProjectTeam> res = projectTeamRepository.findAllByProjectName("");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByProjectName_shouldReturnMultipleTeams() {
    Team secondTeam = createTeam(user);
    ProjectTeam secondProjectTeam = createProjectTeam(project, secondTeam);
    entityManager.persist(secondTeam);
    entityManager.persist(secondProjectTeam);
    entityManager.flush();

    List<ProjectTeam> res = projectTeamRepository.findAllByProjectName(projectName);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(pt -> pt.getTeam().getId().equals(teamId)));
    assertTrue(res.stream().anyMatch(pt -> pt.getTeam().getId().equals(secondTeam.getId())));
  }

  @Test
  public void findAllByTeamName_shouldReturnProjectTeams() {
    List<ProjectTeam> res = projectTeamRepository.findAllByTeamName(teamName);
    assertEquals(1, res.size());
    assertEquals(projectId, res.get(0).getProject().getId());
    assertEquals(teamId, res.get(0).getTeam().getId());
  }

  @Test
  public void findAllByTeamName_shouldReturnEmpty() {
    List<ProjectTeam> res = projectTeamRepository.findAllByTeamName("NonExistent");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByTeamName_shouldHandleEmptyTeamName() {
    List<ProjectTeam> res = projectTeamRepository.findAllByTeamName("");
    assertTrue(res.isEmpty());
  }

  @Test
  public void findAllByTeamName_shouldReturnMultipleProjects() {
    Project secondProject = createProject(user);
    ProjectTeam secondProjectTeam = createProjectTeam(secondProject, team);
    entityManager.persist(secondProject);
    entityManager.persist(secondProjectTeam);
    entityManager.flush();

    List<ProjectTeam> res = projectTeamRepository.findAllByTeamName(teamName);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(pt -> pt.getProject().getId().equals(projectId)));
    assertTrue(res.stream().anyMatch(pt -> pt.getProject().getId().equals(secondProject.getId())));
  }

  @Test
  public void existsByProjectNameAndTeamName_shouldReturnTrue() {
    boolean exists = projectTeamRepository.existsByProjectNameAndTeamName(projectName, teamName);
    assertTrue(exists);
  }

  @Test
  public void existsByProjectNameAndTeamName_shouldReturnFalseForNonExistentProject() {
    boolean exists = projectTeamRepository.existsByProjectNameAndTeamName("NonExistent", teamName);
    assertFalse(exists);
  }

  @Test
  public void existsByProjectNameAndTeamName_shouldReturnFalseForNonExistentTeam() {
    boolean exists = projectTeamRepository.existsByProjectNameAndTeamName(projectName, "NonExistent");
    assertFalse(exists);
  }

  @Test
  public void existsByProjectNameAndTeamName_shouldHandleEmptyNames() {
    boolean exists = projectTeamRepository.existsByProjectNameAndTeamName("", "");
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

  private Project createProject(User creator) {
    Project p = new Project();
    p.setName("My Project Name" + System.nanoTime());
    p.setDescription("Some description");
    p.setCreator(creator);
    p.setCreatedAt(now());
    entityManager.persist(p);
    return p;
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

  private ProjectTeam createProjectTeam(Project project, Team team) {
    ProjectTeam pt = new ProjectTeam();
    pt.setId(new ProjectTeamId(project.getId(), team.getId()));
    pt.setProject(project);
    pt.setTeam(team);
    return pt;
  }
}