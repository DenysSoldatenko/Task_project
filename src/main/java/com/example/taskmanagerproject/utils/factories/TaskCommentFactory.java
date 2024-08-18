package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.entities.task.TaskComment;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
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

    return buildTaskComment(taskCommentDto, task, sender, receiver);
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
   * @return A new TaskComment entity.
   */
  private TaskComment buildTaskComment(TaskCommentDto taskCommentDto, Task task, User sender, User receiver) {
    return TaskComment.builder()
      .task(task)
      .sender(sender)
      .receiver(receiver)
      .message(taskCommentDto.message())
      .createdAt(now())
      .isResolved(taskCommentDto.isResolved())
      .build();
  }
}
