package com.example.taskmanagerproject.utils.achievements;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

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
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for processing task completion events and awarding achievements to users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementConsumer {

  private static final String ACHIEVEMENT_TOPIC = "achievement-topic";
  private static final String ACHIEVEMENT_GROUP = "achievement-group";

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;
  private final TaskCommentRepository taskCommentRepository;
  private final AchievementRepository achievementRepository;
  private final AchievementsUsersRepository achievementsUsersRepository;

  /**
   * Listens for task completion events and determines if an achievement should be awarded.
   *
   * @param event the Kafka event containing task completion details.
   */
  @KafkaListener(topics = ACHIEVEMENT_TOPIC, groupId = ACHIEVEMENT_GROUP)
  public void processTaskCompletion(KafkaTaskCompletionDto event) {
    log.info("Received task completion event for user: {}", event.userId());

    User user = userRepository.findById(event.userId()).orElse(null);
    Team team = teamRepository.findById(event.teamId()).orElse(null);
    Project project = projectRepository.findById(event.projectId()).orElse(null);

    if (user == null || project == null || team == null) {
      return;
    }

    List<Achievement> achievements = achievementRepository.findAll();
    long taskCount = taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).size();
    for (Achievement achievement : achievements) {
      if (isAchievementUnlocked(achievement, event, taskCount)) {
        awardAchievement(user, project, team, event, achievement);
      }
    }
  }

  private boolean isAchievementUnlocked(Achievement achievement, KafkaTaskCompletionDto event, long taskCount) {
    return switch (achievement.getTitle()) {
      // Milestone Achievements
      case "First Milestone" -> taskCount >= 10;
      case "Second Milestone" -> taskCount >= 100;
      case "Third Milestone" -> taskCount >= 500;
      case "Master of Tasks" -> taskCount >= 1000;

      // Task-Based Achievements
      case "Consistent Closer" -> countTasksInLast30Days(event) >= 30;
      case "Deadline Crusher" -> countTasksBeforeDeadline(event) >= 20;
      case "Critical Thinker" -> countHighPriorityTasks(event) >= 20;
      case "Stability Savior" -> countCriticalPriorityTasks(event) >= 40;
      case "Task Warrior" -> countTasksCompletedPerDay(event) >= 5;
      case "Rejection Survivor" -> countApprovedAfterRejection(event) >= 10;

      // Bug Fixing & Issue Resolution
      case "Bug Slayer" -> countFixedCriticalBugsInOneMonth(event) >= 20;
      case "Code Doctor" -> countFixedBugs(event) >= 100;
      case "Bug Bounty Hunter" -> countReportedBugs(event) >= 25;
      case "Quality Champion" -> countResolvedReviewComments(event) >= 30;
//
//      // Time Management
//      case "Time Wizard" -> checkFasterCompletion(user);
//      case "Never Late" -> countOnTimeTasks(user, event) >= 50;
//      case "On-Time Achiever" -> checkOnTimeCompletionRate(user, event) >= 90;
//      case "Deadline Hero" -> checkUrgentTasks(user) >= 1;
//      case "Last-Minute Savior" -> checkLastMinuteSave(user);
//
//      // Teamwork & Collaboration
//      case "Team Player" -> countTeamProjects(user) >= 5;
//      case "Mentor" -> countHelpedTeammates(user) >= 10;
//      case "Knowledge Sharer" -> countHelpfulComments(user) >= 15;
//      case "Support System" -> countReviewedTasks(user) >= 20;
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

  // Helper Methods for Achievements
  private long countTasksInLast30Days(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> task.getApprovedAt().isAfter(now().minusDays(30)))
      .count();
  }

  private long countTasksBeforeDeadline(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> task.getExpirationDate().isAfter(task.getApprovedAt()))
      .count();
  }

  private long countHighPriorityTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> "HIGH".equals(task.getPriority().name()))
      .count();
  }

  private long countCriticalPriorityTasks(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .count();
  }

  private long countTasksCompletedPerDay(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .collect(groupingBy(task -> task.getApprovedAt().toLocalDate(), counting()))
      .values().stream()
      .filter(count -> count >= 5)
      .count();
  }

  private long countApprovedAfterRejection(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> taskRepository.hasTaskBeenCancelled(task.getId()))
      .count();
  }

  private long countFixedCriticalBugsInOneMonth(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()))
      .collect(groupingBy(task -> YearMonth.from(task.getApprovedAt()), counting()))
      .values().stream()
      .max(Long::compare)
      .orElse(0L);
  }

  private long countFixedBugs(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count();
  }

  private long countReportedBugs(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedByUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> "CRITICAL".equals(task.getPriority().name()) && taskCommentRepository.existsByTaskId(task.getId()))
      .count();
  }

  private long countResolvedReviewComments(KafkaTaskCompletionDto event) {
    return taskRepository.findAllCompletedTasksAssignedToUser(event.userId(), event.projectId(), event.teamId()).stream()
      .filter(task -> taskCommentRepository.existsByTaskId(task.getId()))
      .count();
  }

}
