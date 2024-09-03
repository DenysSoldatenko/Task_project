package com.example.taskmanagerproject.utils.factories;

import static java.util.stream.Collectors.toSet;

import com.example.taskmanagerproject.dtos.tasks.KafkaTaskCompletionDto;
import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsers;
import com.example.taskmanagerproject.entities.achievements.AchievementsUsersId;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.AchievementRepository;
import com.example.taskmanagerproject.repositories.AchievementsUsersRepository;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.AchievementMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory class responsible for evaluating and assigning achievements to users based on task completion events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class AchievementFactory {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;
  private final AchievementRepository achievementRepository;
  private final AchievementMetricsService achievementMetricsService;
  private final AchievementsUsersRepository achievementsUsersRepository;

  /**
   * Evaluates and assigns achievements based on a task completion event.
   *
   * @param event The task completion event.
   */
  public void evaluateAchievements(KafkaTaskCompletionDto event) {
    var user = userRepository.findById(event.userId());
    var team = teamRepository.findById(event.teamId());
    var project = projectRepository.findById(event.projectId());

    if (user.isEmpty() || team.isEmpty() || project.isEmpty()) {
      return;
    }

    var existingAchievements = achievementsUsersRepository.findAllByUserId(event.userId()).stream()
        .map(a -> a.getAchievement().getTitle())
        .collect(toSet());
    var taskCount = achievementMetricsService.countApprovedTasks(event);

    achievementRepository.findAll().stream()
      .filter(a -> !existingAchievements.contains(a.getTitle()) && isAchievementUnlocked(a.getTitle(), event, taskCount))
      .forEach(a -> awardAchievement(user.get(), project.get(), team.get(), event, a));
  }

  private boolean isAchievementUnlocked(String title, KafkaTaskCompletionDto event, long taskCount) {
    return switch (title) {
      // Milestone Achievements
      case "First Milestone" -> taskCount >= 10;
      case "Second Milestone" -> taskCount >= 100;
      case "Third Milestone" -> taskCount >= 500;
      case "Master of Tasks" -> taskCount >= 1000;
      case "Legendary Contributor" -> taskCount >= 2000;

      // Task-Based Achievements
      case "Consistent Closer" -> achievementMetricsService.hasApprovedTasksInLast30Days(event);
      case "Deadline Crusher" -> achievementMetricsService.hasApprovedTasksBeforeDeadline(event);
      case "Critical Thinker" -> achievementMetricsService.hasApprovedHighPriorityTasks(event);
      case "Stability Savior" -> achievementMetricsService.hasApprovedCriticalPriorityTasks(event);
      case "Task Warrior" -> achievementMetricsService.hasApprovedTasksDaily(event);
      case "Rejection Survivor" -> achievementMetricsService.hasTasksApprovedAfterRejection(event);

      // Bug Fixing & Issue Resolution
      case "Bug Slayer" -> achievementMetricsService.hasFixedCriticalBugsInOneMonth(event);
      case "Code Doctor" -> achievementMetricsService.hasFixedBugs(event);
      case "Bug Bounty Hunter" -> achievementMetricsService.hasReportedBugs(event);
      case "Quality Champion" -> achievementMetricsService.hasResolvedReviewComments(event);

      // Time Management
      case "Time Wizard" -> achievementMetricsService.hasApprovedTasks10PercentFaster(event);
      case "On-Time Achiever" -> achievementMetricsService.hasMaintained90PercentOnTimeApprovalRate(event);
      case "Deadline Hero" -> achievementMetricsService.hasApprovedCriticalTaskWithin24Hours(event);
      case "Last-Minute Savior" -> achievementMetricsService.hasSavedProjectByApprovingTaskJustBeforeDeadline(event);

      // Teamwork & Advanced Achievements
      case "Team Player" -> achievementMetricsService.hasCollaboratedWithMultipleTeams(event);
      case "Long-Term Strategist" -> achievementMetricsService.hasWorkedContinuouslyFor6Months(event);
      case "Marathon Worker" -> achievementMetricsService.hasCompletedLongDurationTasks(event);
      case "Task Champion" -> achievementMetricsService.hasMaintained90PercentCompletionFor12Months(event);
      default -> false;
    };
  }

  private void awardAchievement(User user, Project project, Team team, KafkaTaskCompletionDto event, Achievement achievement) {
    boolean alreadyAwarded = achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(
        event.userId(), event.teamId(), event.projectId(), achievement.getId()
    );
    if (!alreadyAwarded) {
      AchievementsUsers achievementsUsers = new AchievementsUsers();
      achievementsUsers.setId(new AchievementsUsersId(event.userId(), achievement.getId()));
      achievementsUsers.setAchievement(achievement);
      achievementsUsers.setUser(user);
      achievementsUsers.setTeam(team);
      achievementsUsers.setProject(project);
      achievementsUsersRepository.save(achievementsUsers);
      log.info("Awarded achievement '{}' to user {}", achievement.getTitle(), user.getId());
    }
  }
}
