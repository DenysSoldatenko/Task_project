package com.example.taskmanagerproject.repositories;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.IN_PROGRESS;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
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

/**
 * Integration tests for the {@link TaskHistoryRepository} interface.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Retrieving task histories by new task status value</li>
 *   <li>Handling multiple task histories for the same status</li>
 * </ul>
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class TaskHistoryRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TaskHistoryRepository taskHistoryRepository;

  private Task task;
  private Long taskId;
  private Long historyId;

  @BeforeEach
  void setUp() {
    User sender = createUser();
    User receiver = createUser();

    Team team = createTeam(sender);
    Project project = createProject(sender);

    task = createTask(team, project, sender, receiver);
    taskId = task.getId();

    TaskHistory taskHistory = createTaskHistory(task);
    historyId = taskHistory.getId();

    entityManager.flush();
  }

  @Test
  public void findAllByNewValue_shouldReturnTaskHistories() {
    List<TaskHistory> res = taskHistoryRepository.findAllByNewValue(IN_PROGRESS);
    assertEquals(1, res.size());
    assertEquals(historyId, res.get(0).getId());
    assertEquals(IN_PROGRESS, res.get(0).getNewValue());
    assertEquals(taskId, res.get(0).getTask().getId());
  }

  @Test
  public void findAllByNewValue_shouldHandleMultipleHistories() {
    TaskHistory secondHistory = createTaskHistory(task);
    entityManager.persist(secondHistory);
    entityManager.flush();

    List<TaskHistory> res = taskHistoryRepository.findAllByNewValue(IN_PROGRESS);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(h -> h.getId().equals(historyId)));
    assertTrue(res.stream().anyMatch(h -> h.getId().equals(secondHistory.getId())));
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

  private TaskHistory createTaskHistory(Task task) {
    TaskHistory th = new TaskHistory();
    th.setTask(task);
    th.setNewValue(IN_PROGRESS);
    th.setUpdatedAt(now());
    entityManager.persist(th);
    return th;
  }
}