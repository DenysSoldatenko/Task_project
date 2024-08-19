package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.TASK_COMMENT_FOUND_WITH_ID;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import com.example.taskmanagerproject.entities.task.TaskComment;
import com.example.taskmanagerproject.exceptions.TaskCommentNotFoundException;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.services.TaskCommentService;
import com.example.taskmanagerproject.utils.factories.TaskCommentFactory;
import com.example.taskmanagerproject.utils.mappers.TaskCommentMapper;
import com.example.taskmanagerproject.utils.validators.TaskCommentValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the TaskCommentService interface.
 * This class contains the business logic for managing task comments.
 */
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

  private final TaskCommentMapper taskCommentMapper;
  private final TaskCommentFactory taskCommentFactory;
  private final TaskCommentValidator taskCommentValidator;
  private final TaskCommentRepository taskCommentRepository;

  @Override
  @Transactional
  public TaskCommentDto createComment(TaskCommentDto taskCommentDto) {
    taskCommentValidator.validateTaskCommentDto(taskCommentDto);
    TaskComment createdTaskComment = taskCommentFactory.createTaskCommentFromDto(taskCommentDto);
    taskCommentRepository.save(createdTaskComment);
    return taskCommentMapper.toDto(createdTaskComment);
  }

  @Override
  @Transactional
  public TaskCommentDto updateTaskComment(TaskCommentDto taskCommentDto, Long id) {
    TaskComment existingComment = taskCommentRepository.findById(id)
        .orElseThrow(() -> new TaskCommentNotFoundException(TASK_COMMENT_FOUND_WITH_ID + id));

    existingComment.setMessage(taskCommentDto.message());
    existingComment.setIsResolved(taskCommentDto.isResolved());

    taskCommentRepository.save(existingComment);
    return taskCommentMapper.toDto(existingComment);
  }

  @Override
  @Transactional
  public void deleteTaskComment(Long id) {
    taskCommentRepository.findById(id)
        .orElseThrow(() -> new TaskCommentNotFoundException(TASK_COMMENT_FOUND_WITH_ID + id));
    taskCommentRepository.deleteById(id);
  }

  @Override
  @Transactional
  public List<TaskCommentDto> getCommentsByTaskSlug(String slug) {
    List<TaskComment> taskComments = taskCommentRepository.findAllBySlug(slug);
    return taskComments.stream().map(taskCommentMapper::toDto).toList();
  }
}
