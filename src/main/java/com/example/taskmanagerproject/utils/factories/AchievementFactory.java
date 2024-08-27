package com.example.taskmanagerproject.utils.factories;

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
import com.example.taskmanagerproject.services.TaskMetricsService;
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
  private final TaskMetricsService taskMetricsService;
  private final AchievementRepository achievementRepository;
  private final AchievementsUsersRepository achievementsUsersRepository;

  /**
   * Evaluates whether a user has unlocked any achievements based on a task completion event.
   *
   * @param event The task completion event.
   */
  public void evaluateAchievements(KafkaTaskCompletionDto event) {
    User user = userRepository.findById(event.userId()).orElse(null);
    Team team = teamRepository.findById(event.teamId()).orElse(null);
    Project project = projectRepository.findById(event.projectId()).orElse(null);

    if (user == null || team == null || project == null) {
      return;
    }

    long taskCount = taskMetricsService.countCompletedTasks(event);
    achievementRepository.findAll()
      .stream()
      .filter(achievement -> isAchievementUnlocked(achievement, event, taskCount))
        .forEach(achievement -> awardAchievement(user, project, team, event, achievement));
  }

  private boolean isAchievementUnlocked(Achievement achievement, KafkaTaskCompletionDto event, long taskCount) {
    return switch (achievement.getTitle()) {
      // Milestone Achievements
      case "First Milestone" -> taskCount >= 10;
      case "Second Milestone" -> taskCount >= 100;
      case "Third Milestone" -> taskCount >= 500;
      case "Master of Tasks" -> taskCount >= 1000;

      // Task-Based Achievements
      case "Consistent Closer" -> taskMetricsService.countTasksInLast30Days(event);
      case "Deadline Crusher" -> taskMetricsService.countTasksBeforeDeadline(event);
      case "Critical Thinker" -> taskMetricsService.countHighPriorityTasks(event);
      case "Stability Savior" -> taskMetricsService.countCriticalPriorityTasks(event);
      case "Task Warrior" -> taskMetricsService.countTasksCompletedPerDay(event);
      case "Rejection Survivor" -> taskMetricsService.countApprovedAfterRejection(event);

      // Bug Fixing & Issue Resolution
      case "Bug Slayer" -> taskMetricsService.countFixedCriticalBugsInOneMonth(event);
      case "Code Doctor" -> taskMetricsService.countFixedBugs(event);
      case "Bug Bounty Hunter" -> taskMetricsService.countReportedBugs(event);
      case "Quality Champion" -> taskMetricsService.countResolvedReviewComments(event);
//
//      // Time Management
//      case "Time Wizard" -> taskMetricsService.checkFasterCompletion(user);
//      case "Never Late" -> taskMetricsService.countOnTimeTasks(user, event) >= 50;
//      case "On-Time Achiever" -> taskMetricsService.checkOnTimeCompletionRate(user, event) >= 90;
//      case "Deadline Hero" -> taskMetricsService.checkUrgentTasks(user) >= 1;
//      case "Last-Minute Savior" -> taskMetricsService.checkLastMinuteSave(user);
//
//      // Teamwork & Collaboration
//      case "Team Player" -> taskMetricsService.countTeamProjects(user) >= 5;
//      case "Mentor" -> taskMetricsService.countHelpedTeammates(user) >= 10;
//      case "Knowledge Sharer" -> taskMetricsService.countHelpfulComments(user) >= 15;
//      case "Support System" -> taskMetricsService.countReviewedTasks(user) >= 20;
//
//      // Commenting & Feedback
//      case "Discussion Leader" -> countDiscussionsStarted(user) >= 20;
//      case "Review Guru" -> countTaskReviews(user) >= 50;
//      case "Question Master" -> countInsightfulQuestions(user) >= 25;
//
//      // Task History & Advanced Achievements
//      case "Long-Term Strategist" -> checkContinuousTaskManagement(user);
//      case "Marathon Worker" -> countLongTasks(user) >= 50;
//      case "Task Champion" -> checkConsistency(user) >= 90;
      case "Legendary Contributor" -> taskCount >= 2000;
      default -> false;
    };
  }

  private void awardAchievement(User user, Project project, Team team, KafkaTaskCompletionDto event, Achievement achievement) {
    boolean alreadyAwarded = achievementsUsersRepository.existsByUserIdAndTeamIdAndProjectIdAndAchievementId(event.userId(), event.teamId(), event.projectId(), achievement.getId());
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
