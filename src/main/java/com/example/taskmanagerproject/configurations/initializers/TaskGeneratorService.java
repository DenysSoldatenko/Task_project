package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.ASSIGNED;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.IN_PROGRESS;
import static java.time.LocalDateTime.now;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.entities.tasks.TaskHistory;
import com.example.taskmanagerproject.entities.tasks.TaskPriority;
import com.example.taskmanagerproject.entities.tasks.TaskStatus;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskHistoryRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
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
  private static final int MIN_TASKS = 10;
  private static final int MAX_TASKS = 21;
  private static final int MIN_COMMENTS = 3;
  private static final int MAX_COMMENTS = 8;
  private static final int MIN_EXPIRATION_DAYS = -3;
  private static final int MAX_EXPIRATION_DAYS = 3;

  private final Faker faker = new Faker();
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final TeamUserRepository teamUserRepository;
  private final TaskCommentRepository taskCommentRepository;
  private final TaskHistoryRepository taskHistoryRepository;

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
        .map(teamUser -> createTasksForUser(teamUser, project))
        .flatMap(Collection::stream)
        .toList()
    );
  }

  private List<Task> createTasksForUser(TeamUser teamUser, Project project) {
    return Optional.ofNullable(getAssignedByUser(teamUser))
      .map(assignedBy -> range(0, RANDOM.nextInt(MAX_TASKS) + MIN_TASKS)
        .mapToObj(i -> createTask(project, teamUser.getTeam(), teamUser.getUser(), assignedBy))
        .toList())
      .orElseGet(List::of);
  }

  private User getAssignedByUser(TeamUser teamUser) {
    return teamUser.getRole().getName().equals("ADMIN")
      ? null
      : teamUserRepository.findRandomHigherRoleUser(teamUser.getTeam().getId(), teamUser.getUser().getId())
      .flatMap(userRepository::findById)
      .orElse(null);
  }

  private Task createTask(Project project, Team team, User assignedTo, User assignedBy) {
    TaskStatus randomStatus = Arrays.stream(TaskStatus.values())
        .filter(status -> status != APPROVED)
        .toList()
        .get(RANDOM.nextInt(TaskStatus.values().length - 1)); // -1 to exclude APPROVED

    return Task.builder()
      .project(project)
      .team(team)
      .title(faker.lorem().sentence(3))
      .description(faker.lorem().paragraph())
      .taskStatus(randomStatus)
      .priority(faker.options().option(TaskPriority.values()))
      .assignedTo(assignedTo)
      .assignedBy(assignedBy)
      .createdAt(now().minusDays(RANDOM.nextInt(7)))
      .expirationDate(now().plusDays(RANDOM.nextInt(7)))
      .build();
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
      .map(t -> range(0, RANDOM.nextInt(MAX_COMMENTS) + MIN_COMMENTS)
        .mapToObj(i -> createTaskComment(t, i))
        .toList())
      .map(taskCommentRepository::saveAll)
      .orElseGet(List::of);
  }

  private TaskComment createTaskComment(Task task, int index) {
    User sender = index % 2 == 0 ? task.getAssignedBy() : task.getAssignedTo();
    User receiver = sender.equals(task.getAssignedBy()) ? task.getAssignedTo() : task.getAssignedBy();

    return TaskComment.builder()
      .task(task)
      .sender(sender)
      .receiver(receiver)
      .slug("task-" + task.getId())
      .message(faker.lorem().sentence(5) + " (Message #" + (index + 1) + ")")
      .createdAt(now())
      .isResolved(index == MAX_COMMENTS - 1 && RANDOM.nextBoolean())
      .build();
  }

  /**
   * Changes the status of tasks assigned to all users, setting those with IDs divisible by 2 to APPROVED.
   * Skips tasks that are already marked as APPROVED, ASSIGNED, or IN_PROGRESS.
   *
   * @return the number of tasks whose status was updated to APPROVED
   */
  public int changeTaskStatusForAllUsers() {
    List<Task> tasksToApprove = taskRepository.findAll().stream()
        .filter(task -> task.getId() % 2 == 0 && !EnumSet.of(APPROVED, ASSIGNED, IN_PROGRESS).contains(task.getTaskStatus()))
        .peek(task -> {
          task.setTaskStatus(APPROVED);
          task.setApprovedAt(updateApprovedAt(task));
        })
        .toList();

    taskRepository.saveAll(tasksToApprove);
    return tasksToApprove.size();
  }

  private LocalDateTime updateApprovedAt(Task task) {
    LocalDateTime approvedAt = task.getExpirationDate().plusDays(RANDOM.nextInt(MAX_EXPIRATION_DAYS) + MIN_EXPIRATION_DAYS);
    return approvedAt.isBefore(task.getCreatedAt().plusMinutes(10)) ? task.getCreatedAt().plusMinutes(10) : approvedAt;
  }

  /**
   * Updates the "updatedAt" field for all task histories with the status "APPROVED".
   *
   * @return the number of task histories that were updated
   */
  public int updateTaskHistoryUpdatedAtForAllUsers() {
    List<TaskHistory> updatedHistories = taskHistoryRepository.findAll().stream()
        .filter(history -> history.getNewValue() == APPROVED)
        .peek(this::updateUpdatedAt)
        .toList();

    taskHistoryRepository.saveAll(updatedHistories);
    return updatedHistories.size();
  }

  private void updateUpdatedAt(TaskHistory taskHistory) {
    LocalDateTime baseDate = taskHistory.getTask().getApprovedAt();
    taskHistory.setUpdatedAt(baseDate);
  }
}
