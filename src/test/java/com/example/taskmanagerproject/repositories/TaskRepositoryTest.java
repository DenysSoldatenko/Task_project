package com.example.taskmanagerproject.repositories;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskPriority.LOW;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.CANCELLED;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.projects.ProjectTeamId;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class TaskRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TaskRepository taskRepository;

  private Task task;
  private Long teamId;
  private Long taskId;
  private Long projectId;
  private User firstUser;
  private User secondUser;
  private String teamName;
  private Long firstUserId;
  private String projectName;
  private LocalDateTime endDate;
  private LocalDateTime startDate;

  @BeforeEach
  void setUp() {
    firstUser = createUser();
    secondUser = createUser();
    firstUserId = firstUser.getId();

    Team team = createTeam(firstUser);
    teamId = team.getId();
    teamName = team.getName();

    Project project = createProject(firstUser);
    projectId = project.getId();
    projectName = project.getName();

    Role role = createRole();
    TeamUser teamUser = createTeamUser(firstUser, team, role);
    entityManager.persist(teamUser);

    ProjectTeam projectTeam = createProjectTeam(project, team);
    entityManager.persist(projectTeam);

    startDate = now().minusDays(30);
    endDate = now();

    task = createTask(team, project, firstUser, secondUser);
    taskId = task.getId();
    task.setCreatedAt(now().minusDays(1));
    task.setTaskStatus(APPROVED);
    task.setExpirationDate(now());
    task.setApprovedAt(now().minusDays(1));
    task.setPriority(CRITICAL);
    entityManager.persist(task);

    TaskComment comment = createTaskComment(firstUser, secondUser, task);
    entityManager.persist(comment);

    entityManager.flush();
  }

  @Test
  public void getTaskMetricsByAssignedUser_shouldReturnMetrics() {
    List<Object[]> res = taskRepository.getTaskMetricsByAssignedUser(firstUserId, startDate, endDate, projectName, teamName);
    assertEquals(1, res.size());
    Object[] metrics = res.get(0);
    assertEquals(projectId, metrics[0]);
    assertEquals(teamId, metrics[1]);
    assertEquals("MEMBER", metrics[2]);
    assertEquals(1L, metrics[3]); // allTasks
    assertEquals(1L, metrics[4]); // tasksCompleted
    assertEquals(100.0, ((BigDecimal) metrics[5]).doubleValue(), 0.01); // taskCompletionRate
    assertEquals(1L, metrics[6]); // onTimeTasks
    assertEquals(1L, metrics[7]); // allBugs
    assertEquals(1L, metrics[8]); // bugFixesResolved
    assertEquals(1L, metrics[9]); // allCriticalTasks
    assertEquals(1L, metrics[10]); // criticalTasksSolved
  }

  @Test
  public void getTaskMetricsByAssignedUser_shouldReturnEmpty() {
    List<Object[]> res = taskRepository.getTaskMetricsByAssignedUser(999L, startDate, endDate, projectName, teamName);
    assertTrue(res.isEmpty());
  }

  @Test
  public void getTopPerformerMetricsByTeamName_shouldReturnMetrics() {
    List<Object[]> res = taskRepository.getTopPerformerMetricsByTeamName(teamName, projectName, startDate.minusHours(1), endDate);
    Object[] metrics = res.get(0);
    assertEquals(firstUser.getFullName(), metrics[0]);
    assertEquals(1L, metrics[3]); // all_tasks
    assertEquals(1L, metrics[4]); // tasks_completed
    assertEquals(100.0, ((BigDecimal) metrics[5]).doubleValue(), 0.01); // task_completion_rate
  }

  @Test
  public void getProjectMetricsByProjectName_shouldReturnMetrics() {
    List<Object[]> res = taskRepository.getProjectMetricsByProjectName(projectName, startDate, endDate);
    assertEquals(1, res.size());
    Object[] metrics = res.get(0);
    assertEquals(teamName, metrics[0]);
    assertEquals(0L, metrics[1]); // achievement_count
    assertEquals(1L, metrics[2]); // total_tasks
    assertEquals(1L, metrics[3]); // completed_tasks
    assertEquals(1L, metrics[4]); // on_time_tasks
    assertEquals(1L, metrics[5]); // total_critical_tasks
    assertEquals(1L, metrics[6]); // critical_tasks_completed
    assertEquals(1L, metrics[7]); // total_bugs
    assertEquals(1L, metrics[8]); // bugs_completed
  }

  @Test
  public void getProjectMetricsByProjectName_shouldReturnEmpty() {
    List<Object[]> res = taskRepository.getProjectMetricsByProjectName("NonExistent", startDate, endDate);
    assertTrue(res.isEmpty());
  }

  @Test
  public void getAllTeamMemberMetricsByTeamName_shouldReturnMetrics() {
    List<Object[]> res = taskRepository.getAllTeamMemberMetricsByTeamName(teamName, projectName, startDate, endDate);
    assertEquals(1, res.size());
    Object[] metrics = res.get(0);
    assertEquals(secondUser.getFullName(), metrics[0]);
    assertEquals(1L, metrics[3]); // all_tasks
    assertEquals(1L, metrics[4]); // tasks_completed
    assertEquals(1L, metrics[5]); // onTimeTasks
    assertEquals(100.0, ((BigDecimal) metrics[7]).doubleValue(), 0.01); // task_completion_rate
    assertEquals(100.0, ((BigDecimal) metrics[8]).doubleValue(), 0.01); // bugFixResolutionRate
    assertEquals(100.0, ((BigDecimal) metrics[9]).doubleValue(), 0.01); // criticalTaskResolutionRate
  }

  @Test
  public void getAllTeamMemberMetricsByTeamName_shouldHandleNoTasks() {
    entityManager.getEntityManager().createQuery("DELETE FROM Task").executeUpdate();
    entityManager.flush();
    List<Object[]> res = taskRepository.getAllTeamMemberMetricsByTeamName(teamName, projectName, startDate, endDate);
    assertEquals(1, res.size());
    assertEquals(0L, res.get(0)[3]); // all_tasks
  }

  @Test
  public void getDailyCompletionRates_shouldReturnRates() {
    List<Object[]> res = taskRepository.getDailyCompletionRates(startDate.plusDays(29), endDate.minusHours(1), firstUserId, projectName, teamName);
    assertFalse(res.isEmpty());
    Object[] rate = res.get(res.size() - 1);
    assertEquals(100.0, ((BigDecimal) rate[1]).doubleValue(), 0.01);
  }

  @Test
  public void getDailyCompletionRates_shouldReturnZeroRate() {
    entityManager.getEntityManager().createQuery("DELETE FROM Task").executeUpdate();
    entityManager.flush();
    List<Object[]> res = taskRepository.getDailyCompletionRates(startDate, endDate, firstUserId, projectName, teamName);
    assertFalse(res.isEmpty());
    assertEquals(0.0, ((BigDecimal) res.get(0)[1]).doubleValue(), 0.01);
  }

  @Test
  public void getMonthlyCompletionRates_shouldReturnRates() {
    List<Object[]> res = taskRepository.getMonthlyCompletionRates(startDate.plusDays(29), endDate, firstUserId, projectName, teamName);
    assertFalse(res.isEmpty());
    Object[] rate = res.get(res.size() - 1);
    assertEquals(100.0, ((BigDecimal) rate[1]).doubleValue(), 0.01);
  }

  @Test
  public void getMonthlyCompletionRates_shouldReturnZeroRate() {
    entityManager.getEntityManager().createQuery("DELETE FROM Task").executeUpdate();
    entityManager.flush();
    List<Object[]> res = taskRepository.getMonthlyCompletionRates(startDate, endDate, firstUserId, projectName, teamName);
    assertFalse(res.isEmpty());
    assertEquals(0.0, ((BigDecimal) res.get(0)[1]).doubleValue(), 0.01);
  }

  @Test
  public void findTasksAssignedToUser_shouldReturnPagedTasks() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> res = taskRepository.findTasksAssignedToUser(firstUser.getSlug(), projectName, teamName, pageable);
    assertEquals(1, res.getContent().size());
    assertEquals(taskId, res.getContent().get(0).getId());
  }

  @Test
  public void findTasksAssignedToUser_shouldReturnEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> res = taskRepository.findTasksAssignedToUser("nonexistent-slug", projectName, teamName, pageable);
    assertTrue(res.getContent().isEmpty());
  }

  @Test
  public void findTasksAssignedByUser_shouldReturnPagedTasks() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> res = taskRepository.findTasksAssignedByUser(secondUser.getSlug(), projectName, teamName, pageable);
    assertEquals(1, res.getContent().size());
    assertEquals(taskId, res.getContent().get(0).getId());
  }

  @Test
  public void findTasksAssignedByUser_shouldReturnEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> res = taskRepository.findTasksAssignedByUser("nonexistent-slug", projectName, teamName, pageable);
    assertTrue(res.getContent().isEmpty());
  }

  @Test
  public void findAllCompletedTasksAssignedToUser_shouldReturnTasks() {
    List<Task> res = taskRepository.findAllCompletedTasksAssignedToUser(firstUserId, projectId, teamId);
    assertEquals(1, res.size());
    assertEquals(taskId, res.get(0).getId());
    assertEquals(APPROVED, res.get(0).getTaskStatus());
  }

  @Test
  public void findAllCompletedTasksAssignedToUser_shouldReturnEmpty() {
    List<Task> res = taskRepository.findAllCompletedTasksAssignedToUser(999L, projectId, teamId);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findRandomApprovedTasksForUserByTeamAndProject_shouldReturnTasks() {
    List<Task> res = taskRepository.findRandomApprovedTasksForUserByTeamAndProject();
    assertEquals(1, res.size());
    assertEquals(taskId, res.get(0).getId());
    assertEquals(APPROVED, res.get(0).getTaskStatus());
  }

  @Test
  public void findRandomApprovedTasksForUserByTeamAndProject_shouldReturnEmpty() {
    entityManager.getEntityManager().createQuery("DELETE FROM Task").executeUpdate();
    entityManager.flush();
    List<Task> res = taskRepository.findRandomApprovedTasksForUserByTeamAndProject();
    assertTrue(res.isEmpty());
  }

  @Test
  public void findExpiringTasksForUser_shouldReturnTasks() {
    List<Task> res = taskRepository.findExpiringTasksForUser(now().minusDays(1), now().plusDays(1), projectName, teamName, firstUserId);
    assertEquals(1, res.size());
    assertEquals(taskId, res.get(0).getId());
    assertNotNull(res.get(0).getExpirationDate());
  }

  @Test
  public void findExpiringTasksForUser_shouldReturnEmpty() {
    List<Task> res = taskRepository.findExpiringTasksForUser(now().minusDays(1), now().plusDays(1), projectName, teamName, 999L);
    assertTrue(res.isEmpty());
  }

  @Test
  public void hasTaskBeenCancelled_shouldReturnTrue() {
    TaskHistory history = createTaskHistory(task);
    entityManager.persist(history);
    entityManager.flush();

    boolean res = taskRepository.hasTaskBeenCancelled(taskId);
    assertTrue(res);
  }

  @Test
  public void hasTaskBeenCancelled_shouldReturnFalse() {
    boolean res = taskRepository.hasTaskBeenCancelled(taskId);
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

  private Task createTask(Team team, Project project, User receiver, User sender) {
    Task task = new Task();
    task.setTitle("Test Task " + System.nanoTime());
    task.setDescription("Test description");
    task.setCreatedAt(now().minusDays(3));
    task.setTeam(team);
    task.setProject(project);
    task.setAssignedBy(sender);
    task.setAssignedTo(receiver);
    entityManager.persist(task);
    return task;
  }

  private TaskComment createTaskComment(User receiver, User sender, Task task) {
    TaskComment tc = new TaskComment();
    tc.setReceiver(receiver);
    tc.setSender(sender);
    tc.setTask(task);
    tc.setMessage("Some message " + System.nanoTime());
    tc.setSlug("task-slug-" + System.nanoTime());
    return tc;
  }

  private TaskHistory createTaskHistory(Task task) {
    TaskHistory th = new TaskHistory();
    th.setTask(task);
    th.setPreviousValue(CANCELLED);
    th.setUpdatedAt(now());
    return th;
  }

  private ProjectTeam createProjectTeam(Project project, Team team) {
    ProjectTeam pt = new ProjectTeam();
    pt.setId(new ProjectTeamId(project.getId(), team.getId()));
    pt.setProject(project);
    pt.setTeam(team);
    return pt;
  }
}