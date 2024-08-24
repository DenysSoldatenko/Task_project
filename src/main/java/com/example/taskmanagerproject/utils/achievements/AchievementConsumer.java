//package com.example.taskmanagerproject.utils.achievements;
//
//import com.example.taskmanagerproject.entities.users.User;
//import com.example.taskmanagerproject.entities.achievements.Achievement;
//import com.example.taskmanagerproject.repositories.AchievementRepository;
//import com.example.taskmanagerproject.repositories.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AchievementConsumer {
//
//  private final UserRepository userRepository;
//  private final AchievementRepository achievementRepository;
//
//  @KafkaListener(topics = "task-completed", groupId = "achievement-group")
//  public void processTaskCompletion(TaskCompletedEvent event) {
//    log.info("Received task completion event for user: {}", event.getUserId());
//
//    User user = userRepository.findById(event.getUserId()).orElse(null);
//    if (user == null) return;
//
//    List<Achievement> achievements = achievementRepository.findAll();
//    for (Achievement achievement : achievements) {
//      if (isAchievementUnlocked(achievement, user, event)) {
//        awardAchievement(user, achievement);
//      }
//    }
//  }
//
//  private boolean isAchievementUnlocked(Achievement achievement, User user, TaskCompletedEvent event) {
//    long taskCount = user.getCompletedTasks().size();
//
//    return switch (achievement.getTitle()) {
//      // Milestone Achievements
//      case "First Milestone" -> taskCount >= 10;
//      case "Second Milestone" -> taskCount >= 100;
//      case "Third Milestone" -> taskCount >= 500;
//      case "Master of Tasks" -> taskCount >= 1000;
//      case "Legendary Contributor" -> taskCount >= 2000;
//
//      // Task-Based Achievements
//      case "Consistent Closer" -> countTasksInLast30Days(user) >= 30;
//      case "Deadline Crusher" -> countTasksBeforeDeadline(user) >= 20;
//      case "Critical Thinker" -> countHighPriorityTasks(user) >= 20;
//      case "Task Warrior" -> countTasksToday(user) >= 5;
//      case "Rejection Survivor" -> countApprovedAfterRejection(user) >= 10;
//
//      // Bug Fixing & Issue Resolution
//      case "Bug Slayer" -> countFixedCriticalBugs(user) >= 50;
//      case "Code Doctor" -> countFixedBugs(user) >= 100;
//      case "Bug Bounty Hunter" -> countReportedBugs(user) >= 25;
//      case "Quality Champion" -> countResolvedReviewComments(user) >= 30;
//      case "Stability Savior" -> countMajorBugFixes(user) >= 1;
//
//      // Time Management
//      case "Time Wizard" -> checkFasterCompletion(user);
//      case "Never Late" -> countOnTimeTasks(user) >= 50;
//      case "On-Time Achiever" -> checkOnTimeCompletionRate(user) >= 90;
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
//
//      default -> false;
//    };
//  }
//
//  private void awardAchievement(User user, Achievement achievement) {
//    boolean alreadyAwarded = userAchievementRepository.existsByUserAndAchievement(user, achievement);
//    if (!alreadyAwarded) {
//      userAchievementRepository.save(new UserAchievement(user, achievement));
//      log.info("Awarded achievement '{}' to user {}", achievement.getTitle(), user.getId());
//    }
//  }
//
//  // Helper Methods for Achievements
//  private long countTasksInLast30Days(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.getApprovedAt().isAfter(LocalDateTime.now().minusDays(30)))
//      .count();
//  }
//
//  private long countTasksBeforeDeadline(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.getExpirationDate().isAfter(task.getApprovedAt()))
//      .count();
//  }
//
//  private long countHighPriorityTasks(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> "CRITICAL".equals(task.getPriority()))
//      .count();
//  }
//
//  private long countTasksToday(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.getApprovedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
//      .count();
//  }
//
//  private long countApprovedAfterRejection(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(Task::isPreviouslyRejected)
//      .count();
//  }
//
//  private long countFixedCriticalBugs(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.isBugFix() && "CRITICAL".equals(task.getPriority()))
//      .count();
//  }
//
//  private long countFixedBugs(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(Task::isBugFix)
//      .count();
//  }
//
//  private long countReportedBugs(User user) {
//    return user.getReportedIssues().size();
//  }
//
//  private long countResolvedReviewComments(User user) {
//    return user.getResolvedReviewComments().size();
//  }
//
//  private long countMajorBugFixes(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.isMajorBugFix())
//      .count();
//  }
//
//  private long countOnTimeTasks(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.getExpirationDate().isAfter(task.getApprovedAt()))
//      .count();
//  }
//
//  private double checkOnTimeCompletionRate(User user) {
//    long totalTasks = user.getCompletedTasks().size();
//    long onTimeTasks = countOnTimeTasks(user);
//    return totalTasks == 0 ? 0 : (onTimeTasks * 100.0) / totalTasks;
//  }
//
//  private long countTeamProjects(User user) {
//    return user.getTeamProjects().size();
//  }
//
//  private long countHelpedTeammates(User user) {
//    return user.getHelpedTeammates().size();
//  }
//
//  private long countHelpfulComments(User user) {
//    return user.getTaskComments().size();
//  }
//
//  private long countReviewedTasks(User user) {
//    return user.getReviewedTasks().size();
//  }
//
//  private long countDiscussionsStarted(User user) {
//    return user.getDiscussionsStarted().size();
//  }
//
//  private long countTaskReviews(User user) {
//    return user.getTaskReviews().size();
//  }
//
//  private long countInsightfulQuestions(User user) {
//    return user.getInsightfulQuestions().size();
//  }
//
//  private boolean checkContinuousTaskManagement(User user) {
//    return user.getContinuousMonths() >= 6;
//  }
//
//  private long countLongTasks(User user) {
//    return user.getCompletedTasks().stream()
//      .filter(task -> task.getDuration() > 30)
//      .count();
//  }
//
//  private long checkConsistency(User user) {
//    return user.getTaskCompletionConsistency();
//  }
//}
