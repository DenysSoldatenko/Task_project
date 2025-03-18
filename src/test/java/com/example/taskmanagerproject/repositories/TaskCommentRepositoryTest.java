package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
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
public class TaskCommentRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TaskCommentRepository taskCommentRepository;

  private Task task;
  private User sender;
  private User receiver;
  private String taskSlug;
  private Long userId, taskId, commentId;

  @BeforeEach
  void setUp() {
    sender = createUser();
    userId = sender.getId();
    receiver = createUser();

    Team team = createTeam(sender);
    Project project = createProject(sender);
    task = createTask(team, project, sender, receiver);
    taskId = task.getId();

    TaskComment taskComment = createTaskComment(receiver, sender, task);
    taskSlug = taskComment.getSlug();
    commentId = taskComment.getId();

    entityManager.flush();
  }

  @Test
  public void findByTaskSlug_shouldReturnPagedComments() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<TaskComment> res = taskCommentRepository.findByTaskSlug(taskSlug, pageable);
    assertEquals(1, res.getContent().size());
    assertEquals(commentId, res.getContent().get(0).getId());
    assertEquals(taskSlug, res.getContent().get(0).getSlug());
  }

  @Test
  public void findByTaskSlug_shouldReturnEmptyPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<TaskComment> res = taskCommentRepository.findByTaskSlug("nonexistent-slug", pageable);
    assertTrue(res.getContent().isEmpty());
  }

  @Test
  public void findByTaskSlug_shouldHandleEmptySlug() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<TaskComment> res = taskCommentRepository.findByTaskSlug("", pageable);
    assertTrue(res.getContent().isEmpty());
  }

  @Test
  public void findByTaskAndSender_shouldReturnComments() {
    List<TaskComment> res = taskCommentRepository.findByTaskAndSender(task, sender);
    assertEquals(1, res.size());
    assertEquals(commentId, res.get(0).getId());
    assertEquals(userId, res.get(0).getSender().getId());
    assertEquals(taskId, res.get(0).getTask().getId());
  }

  @Test
  public void findByTaskAndSender_shouldReturnEmpty() {
    User nonExistentUser = new User();
    nonExistentUser.setId(999L);
    List<TaskComment> res = taskCommentRepository.findByTaskAndSender(task, nonExistentUser);
    assertTrue(res.isEmpty());
  }

  @Test
  public void findByTaskAndSender_shouldHandleMultipleComments() {
    TaskComment secondComment = createTaskComment(receiver, sender, task);
    entityManager.persist(secondComment);
    entityManager.flush();

    List<TaskComment> res = taskCommentRepository.findByTaskAndSender(task, sender);
    assertEquals(2, res.size());
    assertTrue(res.stream().anyMatch(c -> c.getId().equals(commentId)));
    assertTrue(res.stream().anyMatch(c -> c.getId().equals(secondComment.getId())));
  }

  @Test
  public void existsByTaskId_shouldReturnTrue() {
    boolean exists = taskCommentRepository.existsByTaskId(taskId);
    assertTrue(exists);
  }

  @Test
  public void existsByTaskId_shouldReturnFalse() {
    boolean exists = taskCommentRepository.existsByTaskId(999L);
    assertFalse(exists);
  }

  @Test
  public void findDistinctTaskIdBySlug_shouldReturnTaskId() {
    Long res = taskCommentRepository.findDistinctTaskIdBySlug(taskSlug);
    assertEquals(taskId, res);
  }

  @Test
  public void findDistinctTaskIdBySlug_shouldReturnNull() {
    Long res = taskCommentRepository.findDistinctTaskIdBySlug("nonexistent-slug");
    assertNull(res);
  }

  @Test
  public void findDistinctTaskIdBySlug_shouldHandleEmptySlug() {
    Long res = taskCommentRepository.findDistinctTaskIdBySlug("");
    assertNull(res);
  }

  @Test
  public void findDistinctTaskIdById_shouldReturnTaskId() {
    Long res = taskCommentRepository.findDistinctTaskIdById(commentId);
    assertEquals(taskId, res);
  }

  @Test
  public void findDistinctTaskIdById_shouldReturnNull() {
    Long res = taskCommentRepository.findDistinctTaskIdById(999L);
    assertNull(res);
  }

  private User createUser() {
    User u = new User();
    u.setUsername("testuser" + System.nanoTime() + "@gmail.com");
    u.setFullName("Test User");
    u.setSlug("slug-" + System.nanoTime());
    entityManager.persist(u);
    return u;
  }

  private Task createTask(Team team, Project project, User receiver, User sender) {
    Task task = new Task();
    task.setTitle("Test Task " + System.nanoTime());
    task.setDescription("Test description");
    task.setCreatedAt(LocalDateTime.now());
    task.setPriority(TaskPriority.MEDIUM);
    task.setTeam(team);
    task.setProject(project);
    task.setAssignedBy(sender);
    task.setAssignedTo(receiver);
    entityManager.persist(task);
    return task;
  }

  private TaskComment createTaskComment(User receiver, User sender, Task task) {
    TaskComment tc = new TaskComment();
    tc.setSender(sender);
    tc.setReceiver(receiver);
    tc.setTask(task);
    tc.setMessage("Some message " + System.nanoTime());
    tc.setSlug("task-slug-" + System.nanoTime());
    entityManager.persist(tc);
    return tc;
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
}