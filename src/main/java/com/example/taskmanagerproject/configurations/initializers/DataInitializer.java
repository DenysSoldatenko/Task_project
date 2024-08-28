package com.example.taskmanagerproject.configurations.initializers;

import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes role, user, and project data in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

  private static final int USER_BATCH_SIZE = 10;
  private static final int TOTAL_USERS = 20;
  private static final int TOTAL_PROJECTS = 2;
  private static final int TOTAL_TEAMS = 2;

  private final UserGeneratorService userGeneratorService;
  private final TeamGeneratorService teamGeneratorService;
  private final TaskGeneratorService taskGeneratorService;
  private final ProjectGeneratorService projectGeneratorService;
  private final TaskStatusGeneratorService taskStatusGeneratorService;

  /**
   * Updates task statuses for all users.
   * This method uses the taskGeneratorService to change task statuses.
   */
  @Transactional
  public void updateTaskStatuses() {
    int updatedStatusCount = taskStatusGeneratorService.changeTaskStatusForAllUsers();
    log.info("Updated status for {} tasks.", updatedStatusCount);
  }

  /**
   * Updates task history dates for all users.
   * This method uses the taskGeneratorService
   * to update the task history's updated_at date and generate achievements for users.
   */
  @Transactional
  public void updateTaskHistoryDates() {
    int achievementsGenerated = taskStatusGeneratorService.generateAchievementsForUser();
    log.info("Generated achievements for {} users.", achievementsGenerated);
    int updatedDatesCount = taskStatusGeneratorService.updateTaskHistoryUpdatedAtForAllUsers();
    log.info("Updated dates for {} tasks.", updatedDatesCount);
    log.info("Data initialization completed successfully...");
  }

  /**
   * Initializes roles, users, teams, and projects, then generates tasks.
   */
  @Transactional
  public void initializeTasks() {
    try {
      log.info("Starting data initialization...");

      var admin = userGeneratorService.createGlobalAdminUser();
      var projects = projectGeneratorService.generateProjects(admin, TOTAL_PROJECTS);
      var teams = teamGeneratorService.generateTeams(admin, TOTAL_TEAMS);

      log.info("Generated {} projects and {} teams for global admin.", projects.size(), teams.size());
      range(0, TOTAL_USERS / USER_BATCH_SIZE).forEach(i -> processUserBatch(i, admin, teams.get(i % TOTAL_TEAMS), projects.get(i % TOTAL_PROJECTS)));

    } catch (Exception e) {
      log.error("Data initialization failed: {}", e.getMessage(), e);
    }
  }

  private void processUserBatch(int batchIndex, User admin, Team team, Project project) {
    try {
      var users = userGeneratorService.generateUserBatch(USER_BATCH_SIZE);
      log.info("Inserted user batch {} ({} users).", batchIndex, users.size());

      var teamUsers = teamGeneratorService.generateTeamUsers(users, team);
      teamGeneratorService.addAdminToTeam(admin, team);
      projectGeneratorService.generateProjectTeam(team, project);

      var tasks = taskGeneratorService.generateTasks(project, teamUsers);
      var commentsCount = tasks.stream().mapToInt(task -> taskGeneratorService.generateTaskComment(task).size()).sum();
      log.info("Created {} tasks and {} comments for project {}.", tasks.size(), commentsCount, project.getId());

    } catch (Exception e) {
      log.error("Error in batch {}: {}", batchIndex, e.getMessage(), e);
    }
  }
}
