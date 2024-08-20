package com.example.taskmanagerproject.configurations.initializers;

import static java.time.LocalDateTime.now;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.entities.task.TaskPriority;
import com.example.taskmanagerproject.entities.task.TaskStatus;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.entities.team.TeamUser;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

/**
 * Service for generating task-related data.
 */
@Service
@RequiredArgsConstructor
public class TaskGeneratorService {

  private static final Random RANDOM = new Random();

  private final Faker faker = new Faker();
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final TeamUserRepository teamUserRepository;

  /**
   * Generates a list of tasks for the specified project and team users.
   *
   * @param project the project to associate with the tasks
   * @param teamUsers the list of users in the team
   * @return a list of generated tasks
   */
  public List<Task> generateTasks(Project project, List<TeamUser> teamUsers) {
    return taskRepository.saveAll(
      teamUsers.stream()
        .flatMap(teamUser -> {
          int totalTasks = RANDOM.nextInt(91) + 10; // Generates a number between 10 and 100
          return range(0, totalTasks)
            .mapToObj(i -> createTaskFromTeamUser(teamUser, project))
            .filter(Objects::nonNull)
            .toList()
            .stream();
        })
        .toList()
    );
  }

  private Task createTaskFromTeamUser(TeamUser teamUser, Project project) {
    User assignedTo = teamUser.getUser();
    Optional<User> assignedBy = teamUserRepository.findRandomHigherRoleUser(teamUser.getTeam().getId(), assignedTo.getId())
        .flatMap(userRepository::findById);

    if (teamUser.getRole().getName().equals("ADMIN") || assignedBy.isEmpty()) {
      return null; // Admins don't get assigned tasks
    }

    return createTask(project, teamUser.getTeam(), assignedTo, assignedBy.get());
  }

  private Task createTask(Project project, Team team, User assignedTo, User assignedBy) {
    Task task = new Task();
    task.setProject(project);
    task.setTeam(team);
    task.setTitle(faker.lorem().sentence(3));
    task.setDescription(faker.lorem().paragraph());
    task.setExpirationDate(generateRandomExpirationDate());
    task.setTaskStatus(faker.options().option(TaskStatus.values()));
    task.setPriority(faker.options().option(TaskPriority.values()));
    task.setAssignedTo(assignedTo);
    task.setAssignedBy(assignedBy);
    return task;
  }

  private LocalDateTime generateRandomExpirationDate() {
    return now().minusDays(RANDOM.nextInt(180));
  }
}
