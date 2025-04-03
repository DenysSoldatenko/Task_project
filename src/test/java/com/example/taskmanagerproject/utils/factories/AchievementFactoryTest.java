package com.example.taskmanagerproject.utils.factories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsers;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.AchievementRepository;
import com.example.taskmanagerproject.repositories.AchievementsUsersRepository;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.AchievementMetricsService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementFactoryTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private AchievementRepository achievementRepository;

  @Mock
  private AchievementMetricsService achievementMetricsService;

  @Mock
  private AchievementsUsersRepository achievementsUsersRepository;

  @InjectMocks
  private AchievementFactory achievementFactory;

  private User user;
  private Team team;
  private Project project;
  private Achievement achievement;
  private KafkaTaskCompletionDto event;

  @BeforeEach
  void setUp() {
    event = new KafkaTaskCompletionDto(1L, 1L, 1L, 1L);

    user = new User();
    user.setId(1L);

    team = new Team();
    team.setId(1L);

    project = new Project();
    project.setId(1L);

    achievement = new Achievement();
    achievement.setId(1L);
    achievement.setTitle("First Milestone");
  }

  @Test
  void evaluateAchievements_shouldDoNothingIfUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository, never()).save(any());
  }

  @Test
  void evaluateAchievements_shouldDoNothingIfTeamNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.empty());

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository, never()).save(any());
  }

  @Test
  void evaluateAchievements_shouldDoNothingIfProjectNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(projectRepository.findById(1L)).thenReturn(Optional.empty());

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository, never()).save(any());
  }

  @Test
  void evaluateAchievements_shouldAwardFirstMilestone() {
    AchievementsUsers existingAchievement = new AchievementsUsers();
    existingAchievement.setAchievement(new Achievement());
    existingAchievement.getAchievement().setTitle("Other Achievement");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(achievementsUsersRepository.findAllByUserId(1L)).thenReturn(List.of(existingAchievement));
    when(achievementRepository.findAll()).thenReturn(List.of(achievement));
    when(achievementMetricsService.countApprovedTasks(event)).thenReturn(10L);
    when(achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(1L, 1L, 1L, 1L)).thenReturn(false);

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository).save(any(AchievementsUsers.class));
  }

  @Test
  void evaluateAchievements_shouldNotAwardIfAlreadyAwarded() {
    AchievementsUsers existingAchievement = new AchievementsUsers();
    existingAchievement.setAchievement(achievement);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(achievementsUsersRepository.findAllByUserId(1L)).thenReturn(List.of(existingAchievement));
    when(achievementRepository.findAll()).thenReturn(List.of(achievement));
    when(achievementMetricsService.countApprovedTasks(event)).thenReturn(10L);

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository, never()).save(any());
  }

  @Test
  void evaluateAchievements_shouldAwardTaskWarrior() {
    achievement.setTitle("Task Warrior");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(achievementsUsersRepository.findAllByUserId(1L)).thenReturn(List.of());
    when(achievementRepository.findAll()).thenReturn(List.of(achievement));
    when(achievementMetricsService.hasApprovedTasksDaily(event)).thenReturn(true);
    when(achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(1L, 1L, 1L, 1L)).thenReturn(false);

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository).save(any(AchievementsUsers.class));
  }

  @Test
  void evaluateAchievements_shouldNotAwardUnknownAchievement() {
    achievement.setTitle("Unknown Achievement");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(achievementsUsersRepository.findAllByUserId(1L)).thenReturn(List.of());
    when(achievementRepository.findAll()).thenReturn(List.of(achievement));

    achievementFactory.evaluateAchievements(event);

    verify(achievementsUsersRepository, never()).save(any());
  }
}