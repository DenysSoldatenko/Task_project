package com.example.taskmanagerproject.configurations.initializers;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.project.ProjectTeam;
import com.example.taskmanagerproject.entities.project.ProjectTeamId;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.ProjectTeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

/**
 * Service for generating project-related data.
 */
@Service
@RequiredArgsConstructor
public class ProjectGeneratorService {

  private final Faker faker = new Faker();
  private final ProjectRepository projectRepository;
  private final ProjectTeamRepository projectTeamRepository;

  /**
   * Generates projects for a user, ensuring that only users with allowed roles can create projects.
   *
   * @param user          the user for whom projects are being created
   * @param totalProjects the number of projects to create
   */
  public List<Project> generateProjects(User user, int totalProjects) {
    return projectRepository.saveAll(range(0, totalProjects).mapToObj(i -> createProject(user)).toList());
  }

  /**
   * Generates a new project-team association and saves it to the repository.
   *
   * @param team the {@link Team} to be associated with the project.
   * @param project the {@link Project} to be associated with the team.
   * @return the saved {@link ProjectTeam} entity representing the association between the team and the project.
   */
  public ProjectTeam generateProjectTeam(Team team, Project project) {
    return projectTeamRepository.save(createProjectTeam(team, project));
  }

  private ProjectTeam createProjectTeam(Team team, Project project) {
    ProjectTeam projectTeam = new ProjectTeam();
    projectTeam.setId(new ProjectTeamId(team.getId(), project.getId()));
    projectTeam.setProject(project);
    projectTeam.setTeam(team);
    return projectTeam;
  }

  private Project createProject(User user) {
    Project project = new Project();
    project.setName(faker.company().name() + randomUUID().toString().substring(0, 4));
    project.setDescription(faker.lorem().sentence());
    project.setCreator(user);
    project.setCreatedAt(generateRandomDateTime());
    return project;
  }

  private LocalDateTime generateRandomDateTime() {
    long daysAgo = current().nextLong(0, 365);
    return now().minusDays(daysAgo);
  }
}
