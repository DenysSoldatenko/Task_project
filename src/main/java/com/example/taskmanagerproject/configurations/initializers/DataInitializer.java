package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_FAIL_MESSAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_SUCCESS_MESSAGE;
import static java.util.stream.IntStream.range;

import java.util.concurrent.atomic.AtomicBoolean;
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

  private final UserGeneratorService userGeneratorService;
  private final TeamGeneratorService teamGeneratorService;
  private final TaskGeneratorService taskGeneratorService;
  private final ProjectGeneratorService projectGeneratorService;

  /**
   * Initializes role, user, and project data in the database.
   */
  @Transactional
  public String initData() {
    int userBatchSize = 10;
    int totalUsers = 50;
    int totalProjects = 5;
    int totalTeams = 5;

    AtomicBoolean hasErrors = new AtomicBoolean(false);
    log.info("Starting data initialization...");

    try {
      var globalAdmin = userGeneratorService.createGlobalAdminUser();

      var projectBatch = projectGeneratorService.generateProjects(globalAdmin, totalProjects);
      log.info("Generated and saved {} projects for global admin.", projectBatch.size());

      var teamBatch = teamGeneratorService.generateTeams(globalAdmin, totalTeams);
      log.info("Generated and saved {} teams for global admin.", teamBatch.size());

      range(0, totalUsers / userBatchSize)
          .forEach(batchIndex -> {
            try {
              var userBatch = userGeneratorService.generateUserBatch(userBatchSize);
              log.info("User batch {} inserted successfully. Saved {} users.", batchIndex, userBatch.size());

              var team = teamBatch.get(batchIndex);
              var teamUsers = teamGeneratorService.generateTeamUsers(userBatch, team);
              log.info("Generated and saved {} TeamUser associations for team {}.", teamUsers.size(), team.getId());

              var project = projectBatch.get(batchIndex);
              var projectTeam = projectGeneratorService.generateProjectTeam(team, project);
              log.info("Generated and saved {} ProjectTeam associations for project {}.", 1, project.getId());

              var tasks = taskGeneratorService.generateTasks(project, teamUsers);
              log.info("Generated and saved {} Task associations for project {}.", tasks.size(), project.getId());

            } catch (Exception e) {
              log.error("Error in user batch {}: {}", batchIndex, e.getMessage(), e);
              hasErrors.set(true);
            }
          });
    } catch (Exception e) {
      log.error("Error during data initialization: {}", e.getMessage(), e);
      hasErrors.set(true);
    }

    if (hasErrors.get()) {
      log.warn("Data initialization completed with errors.");
      return DATA_INITIALIZATION_FAIL_MESSAGE;
    } else {
      log.info("Data initialization completed successfully.");
      return DATA_INITIALIZATION_SUCCESS_MESSAGE;
    }
  }
}
