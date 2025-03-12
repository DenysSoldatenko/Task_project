package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
  "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class AchievementRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private AchievementRepository achievementRepository;

  private Long userId, teamId, projectId, achievementId;
  private final String title = "Test Achievement";
  private final String description = "Test Description";

  @BeforeEach
  void setUp() {
    User user = createUser();
    userId = user.getId();
    Team team = createTeam(user);
    teamId = team.getId();
    Project project = createProject(user);
    projectId = project.getId();
    Achievement ach = createAchievement(title, description, "img_url");
    achievementId = ach.getId();

    entityManager.getEntityManager().createNativeQuery("""
      INSERT INTO task_list.achievements_users (achievement_id, user_id, team_id, project_id)
      VALUES (:a, :u, :t, :p)
    """)
      .setParameter("a", achievementId)
      .setParameter("u", userId)
      .setParameter("t", teamId)
      .setParameter("p", projectId)
      .executeUpdate();

    entityManager.flush();
  }

  @Test
  public void save_shouldPersistAchievement() {
    Achievement a = createAchievement("New", "Desc", "url");
    Achievement saved = achievementRepository.save(a);
    assertNotNull(saved.getId());
    assertEquals("New", saved.getTitle());
  }

  @Test
  public void save_shouldThrowWhenTitleNull() {
    Achievement a = new Achievement();
    a.setDescription("x");
    assertThrows(DataIntegrityViolationException.class, () -> achievementRepository.saveAndFlush(a));
  }

  @Test
  public void findById_shouldReturnAchievement() {
    Optional<Achievement> res = achievementRepository.findById(achievementId);
    assertTrue(res.isPresent());
    assertEquals(title, res.get().getTitle());
  }

  @Test
  public void findById_shouldReturnEmpty() {
    assertTrue(achievementRepository.findById(999L).isEmpty());
  }

  @Test
  public void deleteById_shouldRemoveAchievement() {
    achievementRepository.deleteById(achievementId);
    assertTrue(achievementRepository.findById(achievementId).isEmpty());
  }

  @Test
  public void deleteById_shouldNotFailOnMissingId() {
    assertDoesNotThrow(() -> achievementRepository.deleteById(999L));
  }

  @Test
  public void findAchievementsByUserTeamAndProject_shouldReturnMatch() {
    List<Achievement> res = achievementRepository.findAchievementsByUserTeamAndProject(userId, teamId, projectId);
    assertEquals(1, res.size());
    assertEquals(title, res.get(0).getTitle());
  }

  @Test
  public void findAchievementsByUserTeamAndProject_shouldReturnEmptyUser() {
    assertTrue(achievementRepository.findAchievementsByUserTeamAndProject(999L, teamId, projectId).isEmpty());
  }

  @Test
  public void findAchievementsByUserTeamAndProject_shouldReturnEmptyTeam() {
    assertTrue(achievementRepository.findAchievementsByUserTeamAndProject(userId, 999L, projectId).isEmpty());
  }

  @Test
  public void findAchievementsByUserTeamAndProject_shouldReturnEmptyProject() {
    assertTrue(achievementRepository.findAchievementsByUserTeamAndProject(userId, teamId, 999L).isEmpty());
  }

  private User createUser() {
    User user = new User();
    user.setUsername("testuser@gmail.com");
    user.setFullName("Test User");
    user.setSlug("slug-" + System.nanoTime());
    entityManager.persist(user);
    return user;
  }

  private Team createTeam(User creator) {
    Team team = new Team();
    team.setName("My Team Name");
    team.setDescription("Some description");
    team.setCreator(creator);
    team.setCreatedAt(now());
    entityManager.persist(team);
    return team;
  }

  private Project createProject(User creator) {
    Project p = new Project();
    p.setName("My Project Name");
    p.setDescription("Some description");
    p.setCreator(creator);
    p.setCreatedAt(now());
    entityManager.persist(p);
    return p;
  }

  private Achievement createAchievement(String title, String desc, String imageUrl) {
    Achievement a = new Achievement();
    a.setTitle(title);
    a.setDescription(desc);
    a.setImageUrl(imageUrl);
    entityManager.persist(a);
    return a;
  }
}
