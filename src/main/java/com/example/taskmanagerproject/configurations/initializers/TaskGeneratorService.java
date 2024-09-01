package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.ASSIGNED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.CANCELLED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.IN_PROGRESS;
import static java.lang.Math.min;
import static java.time.LocalDateTime.now;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.tasks.TaskStatus;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
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
  private static final int MIN_TASKS = 100;
  private static final int MAX_TASKS = 401;
  private static final int MIN_COMMENTS = 1;
  private static final int MAX_COMMENTS = 5;
  private static final int BATCH_SIZE = 500;

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
    List<Task> tasks = teamUsers.stream()
        .map(user -> getAssignedByUser(user)
          .map(assignedBy -> generateTasksForUser(project, user, assignedBy))
          .orElseGet(List::of))
        .flatMap(List::stream)
        .toList();

    saveInBatches(tasks, taskRepository::saveAll);
    return tasks;
  }

  private List<Task> generateTasksForUser(Project project, TeamUser teamUser, User assignedBy) {
    return range(0, RANDOM.nextInt(MAX_TASKS) + MIN_TASKS)
      .mapToObj(i -> createTask(project, teamUser, assignedBy))
      .toList();
  }

  private Optional<User> getAssignedByUser(TeamUser teamUser) {
    return teamUser.getRole().getName().equals("ADMIN")
      ? Optional.empty()
      : teamUserRepository.findRandomHigherRoleUser(teamUser.getTeam().getId(), teamUser.getUser().getId())
      .flatMap(userRepository::findById);
  }

  private Task createTask(Project project, TeamUser teamUser, User assignedBy) {
    return Task.builder()
      .project(project)
      .team(teamUser.getTeam())
      .title(faker.lorem().sentence(3))
      .description(faker.lorem().paragraph())
      .taskStatus(getRandomTaskStatus())
      .priority(faker.options().option(TaskPriority.values()))
      .assignedTo(teamUser.getUser())
      .assignedBy(assignedBy)
      .createdAt(now().minusDays(RANDOM.nextInt(7)))
      .expirationDate(now().plusDays(RANDOM.nextInt(7)))
      .build();
  }

  private TaskStatus getRandomTaskStatus() {
    return Arrays.stream(TaskStatus.values())
      .filter(status -> status != APPROVED)
      .skip(RANDOM.nextInt(TaskStatus.values().length - 1))
      .findFirst().orElse(ASSIGNED);
  }

  /**
   * Generates a conversation between the assigned by and assigned to users.
   *
   * @param task the task for which the comments are generated
   * @return a list of generated task comments
   */
  public List<TaskComment> generateTaskComments(Task task) {
    if (EnumSet.of(ASSIGNED, IN_PROGRESS, APPROVED, CANCELLED).contains(task.getTaskStatus())) {
      return List.of();
    }

    List<TaskComment> comments = range(0, RANDOM.nextInt(MAX_COMMENTS) + MIN_COMMENTS)
        .mapToObj(i -> createTaskComment(task, i))
        .toList();

    saveInBatches(comments, taskCommentRepository::saveAll);
    return comments;
  }

  private TaskComment createTaskComment(Task task, int index) {
    boolean isEven = index % 2 == 0;
    return TaskComment.builder()
      .task(task)
      .sender(isEven ? task.getAssignedBy() : task.getAssignedTo())
      .receiver(isEven ? task.getAssignedTo() : task.getAssignedBy())
      .slug("task-" + task.getId())
      .message(faker.lorem().sentence(5) + " (Message #" + (index + 1) + ")")
      .createdAt(task.getCreatedAt().plusMinutes((index + 1) * 10L))
      .build();
  }

  private <T> void saveInBatches(List<T> items, Consumer<List<T>> saveFunction) {
    for (int i = 0; i < items.size(); i += BATCH_SIZE) {
      saveFunction.accept(items.subList(i, min(i + BATCH_SIZE, items.size())));
    }
  }
}
