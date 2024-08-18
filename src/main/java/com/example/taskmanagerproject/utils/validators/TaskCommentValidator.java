package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_COMMENT_INVALID_SENDER_RECEIVER;
import static com.example.taskmanagerproject.utils.MessageUtils.TASK_NOT_FOUND_WITH_ID;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import com.example.taskmanagerproject.exceptions.TaskNotFoundException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating task comment data.
 */
@Component
public class TaskCommentValidator extends BaseValidator<TaskCommentDto> {

  private final TaskRepository taskRepository;

  /**
   * Constructs a TaskCommentValidator instance.
   *
   * @param validator      The validator instance used for constraint validation.
   * @param taskRepository The repository for accessing task data.
   */
  public TaskCommentValidator(Validator validator, TaskRepository taskRepository) {
    super(validator);
    this.taskRepository = taskRepository;
  }

  /**
   * Validates a TaskCommentDto object.
   *
   * @param taskCommentDto The TaskCommentDto object to validate.
   */
  public void validateTaskCommentDto(TaskCommentDto taskCommentDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(taskCommentDto, errorMessages);
    validateTaskExists(taskCommentDto);
    validateUsers(taskCommentDto, errorMessages);
    throwIfErrorsExist(errorMessages);
  }

  private void validateUsers(TaskCommentDto taskCommentDto, Set<String> errorMessages) {
    String senderName = taskCommentDto.sender().username();
    String receiverName = taskCommentDto.receiver().username();
    String assignedByUsername = taskCommentDto.task().assignedBy().username();
    String assignedToUsername = taskCommentDto.task().assignedTo().username();

    boolean isValidSenderReceiver =
        (assignedByUsername.equals(senderName) && assignedToUsername.equals(receiverName))
        || (assignedByUsername.equals(receiverName) && assignedToUsername.equals(senderName));

    if (!isValidSenderReceiver) {
      errorMessages.add(TASK_COMMENT_INVALID_SENDER_RECEIVER);
    }
  }

  private void validateTaskExists(TaskCommentDto taskCommentDto) {
    taskRepository.findById(taskCommentDto.task().id())
        .orElseThrow(() -> new TaskNotFoundException(TASK_NOT_FOUND_WITH_ID + taskCommentDto.task().id()));
  }
}
