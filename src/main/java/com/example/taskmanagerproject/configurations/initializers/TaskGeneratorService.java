package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.task.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.task.TaskStatus.ASSIGNED;
import static com.example.taskmanagerproject.entities.task.TaskStatus.IN_PROGRESS;
import static java.time.LocalDateTime.now;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.entities.task.TaskComment;
import com.example.taskmanagerproject.entities.task.TaskPriority;
import com.example.taskmanagerproject.entities.task.TaskStatus;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.entities.team.TeamUser;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
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
  private final TaskCommentRepository taskCommentRepository;

  /**
   * Generates a list of tasks for the specified project and team users.
   *
   * @param project   the project to associate with the tasks
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

  /**
   * Generates a conversation between the assigned by and assigned to users.
   *
   * @param task the task for which the comments are generated
   * @return a list of generated task comments
   */
  public List<TaskComment> generateTaskComment(Task task) {
    return Optional.of(task)
      .filter(t -> !EnumSet.of(ASSIGNED, IN_PROGRESS, APPROVED).contains(t.getTaskStatus()))
      .map(t -> {
        int totalMessages = RANDOM.nextInt(11) + 5; // Generate between 5 to 15 messages
        return range(0, totalMessages)
          .mapToObj(i -> createTaskComment(t, t.getAssignedBy(), t.getAssignedTo(), i, totalMessages))
          .toList();
      })
      .map(taskCommentRepository::saveAll)
      .orElseGet(List::of);
  }

  private TaskComment createTaskComment(Task task, User assignedBy, User assignedTo, int messageIndex, int totalMessages) {
    User sender = messageIndex % 2 == 0 ? assignedBy : assignedTo;
    User receiver = sender.equals(assignedBy) ? assignedTo : assignedBy;
    String message = generateRandomMessage(messageIndex);
    String slug = "task-" + task.getId();
    boolean isResolved = messageIndex == totalMessages - 1 && RANDOM.nextBoolean();

    return TaskComment.builder()
      .task(task)
      .sender(sender)
      .receiver(receiver)
      .slug(slug)
      .message(message)
      .createdAt(now())
      .isResolved(isResolved)
      .build();
  }

  private String generateRandomMessage(int messageIndex) {
    return faker.lorem().sentence(10) + " (Message #" + (messageIndex + 1) + ")";
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
