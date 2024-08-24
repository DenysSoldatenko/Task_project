package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating TaskComment instances.
 */
@Component
@RequiredArgsConstructor
public final class TaskCommentFactory {

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final TaskCommentRepository taskCommentRepository;

  /**
   * Creates a new TaskComment entity from a TaskCommentDto.
   *
   * @param taskCommentDto The TaskCommentDto containing the comment details.
   * @return A new TaskComment entity.
   */
  public TaskComment createTaskCommentFromDto(TaskCommentDto taskCommentDto) {
    Task task = getTaskById(taskCommentDto.task().id());
    User sender = getUserByUsername(taskCommentDto.sender().username());
    User receiver = getUserByUsername(taskCommentDto.receiver().username());
    String slug = generateSlugIfNeeded(task, receiver, sender);
    return buildTaskComment(taskCommentDto, task, sender, receiver, slug);
  }

  /**
   * Retrieves a Task entity based on the task ID.
   *
   * @param taskId The ID of the task to retrieve.
   * @return The Task entity.
   */
  private Task getTaskById(Long taskId) {
    return taskRepository.findById(taskId)
      .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskId));
  }

  /**
   * Retrieves a User entity based on the username.
   *
   * @param username The username of the user to retrieve.
   * @return The User entity.
   */
  private User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  /**
   * Builds a TaskComment entity from the provided details.
   *
   * @param taskCommentDto The TaskCommentDto containing the comment details.
   * @param task           The associated task.
   * @param sender         The user who sent the comment.
   * @param receiver       The user who receives the comment.
   * @param slug           The unique slug for the comment.
   * @return A new TaskComment entity.
   */
  private TaskComment buildTaskComment(TaskCommentDto taskCommentDto, Task task, User sender, User receiver, String slug) {
    return TaskComment.builder()
      .task(task)
      .sender(sender)
      .receiver(receiver)
      .slug(slug)
      .message(taskCommentDto.message())
      .createdAt(now())
      .isResolved(taskCommentDto.isResolved())
      .build();
  }

  private String generateSlugIfNeeded(Task task, User receiver, User sender) {
    TaskComment existingComment = taskCommentRepository.findByTaskAndSender(task, sender).stream().findFirst().orElse(
        taskCommentRepository.findByTaskAndSender(task, receiver).stream().findFirst().orElse(null)
    );
    return existingComment != null ? existingComment.getSlug() : generateSlug(task);
  }

  /**
   * Generates a unique slug based on the task.
   *
   * @param task   The task associated with the comment.
   * @return A unique slug for the task comment.
   */
  private String generateSlug(Task task) {
    return "task-" + task.getId() + "-" + randomUUID().toString().substring(0, 8);
  }
}
