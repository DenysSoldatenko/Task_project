package com.example.taskmanagerproject.repositories;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsers;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsersId;
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
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:tc:postgresql:15:///testdb?TC_INITSCRIPT=init-schema.sql",
  "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public class AchievementsUsersRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private AchievementsUsersRepository achievementsUsersRepository;

  private Long userId, teamId, projectId, achievementId;
  private AchievementsUsersId achievementsUsersId;

  @BeforeEach
  void setUp() {
    User user = createUser();
    userId = user.getId();

    Team team = createTeam(user);
    teamId = team.getId();

    Project project = createProject(user);
    projectId = project.getId();

    Achievement achievement = createAchievement();
    achievementId = achievement.getId();

    AchievementsUsers au = new AchievementsUsers();
    achievementsUsersId = new AchievementsUsersId(userId, achievementId);
    au.setId(achievementsUsersId);
    au.setUser(user);
    au.setAchievement(achievement);
    au.setTeam(team);
    au.setProject(project);

    entityManager.persist(au);
    entityManager.flush();
  }

  @Test
  public void findById_shouldReturnAchievementsUsers() {
    Optional<AchievementsUsers> res = achievementsUsersRepository.findById(achievementsUsersId);
    assertTrue(res.isPresent());
    assertEquals(achievementsUsersId, res.get().getId());
  }

  @Test
  public void findById_shouldReturnEmpty() {
    AchievementsUsersId nonExistentId = new AchievementsUsersId(999L, 999L);
    assertTrue(achievementsUsersRepository.findById(nonExistentId).isEmpty());
  }

  @Test
  public void deleteById_shouldRemoveAchievementsUsers() {
    achievementsUsersRepository.deleteById(achievementsUsersId);
    assertTrue(achievementsUsersRepository.findById(achievementsUsersId).isEmpty());
  }

  @Test
  public void deleteById_shouldNotFailOnMissingId() {
    AchievementsUsersId nonExistentId = new AchievementsUsersId(999L, 999L);
    assertDoesNotThrow(() -> achievementsUsersRepository.deleteById(nonExistentId));
  }

  @Test
  public void existsByUserIdAndTeamIdAndProjectIdAndAchievementId_shouldReturnTrueWhenExists() {
    boolean exists = achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(userId, teamId, projectId, achievementId);
    assertTrue(exists);
  }

  @Test
  public void existsByUserIdAndTeamIdAndProjectIdAndAchievementId_shouldReturnFalseWhenNotExists() {
    boolean exists = achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(999L, teamId, projectId, achievementId);
    assertFalse(exists);
  }

  @Test
  public void findAllByUserId_shouldReturnAchievementsUsers() {
    List<AchievementsUsers> res = achievementsUsersRepository.findAllByUserId(userId);
    assertEquals(1, res.size());
    assertEquals(achievementsUsersId, res.get(0).getId());
  }

  @Test
  public void findAllByUserId_shouldReturnEmpty() {
    List<AchievementsUsers> res = achievementsUsersRepository.findAllByUserId(999L);
    assertTrue(res.isEmpty());
  }

  private User createUser() {
    User user = new User();
    user.setUsername("testuser" + System.nanoTime() + "@gmail.com");
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
    Project project = new Project();
    project.setName("My Project Name");
    project.setDescription("Some description");
    project.setCreator(creator);
    project.setCreatedAt(now());
    entityManager.persist(project);
    return project;
  }

  private Achievement createAchievement() {
    Achievement achievement = new Achievement();
    achievement.setTitle("Test Achievement");
    achievement.setDescription("Test Description");
    achievement.setImageUrl("img_url");
    entityManager.persist(achievement);
    return achievement;
  }
}
