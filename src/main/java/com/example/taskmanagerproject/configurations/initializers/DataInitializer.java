package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_FAIL_MESSAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_SUCCESS_MESSAGE;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.ProjectTeamRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.List;
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
  private final ProjectGeneratorService projectGeneratorService;


  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;
  private final TeamUserRepository teamUserRepository;
  private final ProjectTeamRepository projectTeamRepository;

  /**
   * Initializes role, user, and project data in the database.
   */
  @Transactional
  public String initData() {
    int userBatchSize = 10;
    int totalUsers = 100;
    int totalProjects = 5;
    int totalTeams = 10;

    AtomicBoolean hasErrors = new AtomicBoolean(false);
    log.info("Starting data initialization...");

    try {
      var globalAdmin = userGeneratorService.createGlobalAdminUser();
      userRepository.save(globalAdmin);

      var projectBatch = projectGeneratorService.generateProjects(globalAdmin, totalProjects);
      projectRepository.saveAll(projectBatch);
      log.info("Generated and saved {} projects for global admin.", projectBatch.size());

      var teamBatch = teamGeneratorService.generateTeams(globalAdmin, totalTeams);
      teamRepository.saveAll(teamBatch);
      log.info("Generated and saved {} teams for global admin.", teamBatch.size());

      range(0, totalUsers / userBatchSize)
          .forEach(batchIndex -> {
            try {
              var userBatch = userGeneratorService.generateUserBatch(userBatchSize);
              userRepository.saveAll(userBatch);
              log.info("User batch {} inserted successfully. Saved {} users.", batchIndex, userBatch.size());

              var team = teamBatch.get(batchIndex);
              var teamUsers = teamGeneratorService.generateTeamUsers(userBatch, team);
              teamUserRepository.saveAll(teamUsers);
              log.info("Generated and saved {} TeamUser associations for team {}.", teamUsers.size(), team.getId());

              assignTeamsToProjects(teamBatch, projectBatch, batchIndex);
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

  /**
   * Assigns teams to projects.
   *
   * @param teamBatch The list of teams to associate with users.
   * @param projectBatch The list of projects to associate with teams.
   * @param batchIndex The current batch index to use for selecting teams and projects.
   */
  private void assignTeamsToProjects(List<Team> teamBatch, List<Project> projectBatch, int batchIndex) {
    // Check if the batchIndex is odd
    if (batchIndex % 2 != 0) {
      log.info("Batch index {} is odd, skipping assignment for this iteration.", batchIndex);
      return;
    }

    var team1 = teamBatch.get(batchIndex % teamBatch.size());
    var team2 = teamBatch.get((batchIndex + 1) % teamBatch.size());
    var project = projectBatch.get(batchIndex % projectBatch.size());

    var projectTeams = projectGeneratorService.generateProjectTeam(List.of(team1, team2), project);
    projectTeamRepository.saveAll(projectTeams);
    log.info("Generated and saved {} ProjectTeam associations for project {}.", projectTeams.size(), project.getId());
  }
}
