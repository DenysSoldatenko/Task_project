package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.TASK_COMMENT_FOUND_WITH_ID;

import com.example.taskmanagerproject.dtos.tasks.TaskCommentDto;
import com.example.taskmanagerproject.entities.tasks.TaskComment;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TaskCommentRepository;
import com.example.taskmanagerproject.services.TaskCommentService;
import com.example.taskmanagerproject.utils.factories.TaskCommentFactory;
import com.example.taskmanagerproject.utils.mappers.TaskCommentMapper;
import com.example.taskmanagerproject.utils.validators.TaskCommentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        .orElseThrow(() -> new ResourceNotFoundException(TASK_COMMENT_FOUND_WITH_ID + id));

    existingComment.setMessage(taskCommentDto.message());

    taskCommentRepository.save(existingComment);
    return taskCommentMapper.toDto(existingComment);
  }

  @Override
  @Transactional
  public void deleteTaskComment(Long id) {
    taskCommentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(TASK_COMMENT_FOUND_WITH_ID + id));
    taskCommentRepository.deleteById(id);
  }

  @Override
  @Transactional
  public Page<TaskCommentDto> getCommentsByTaskSlug(String slug, Pageable pageable) {
    Page<TaskComment> taskCommentsPage = taskCommentRepository.findByTaskSlug(slug, pageable);
    return taskCommentsPage.map(taskCommentMapper::toDto);
  }

  @Override
  @Transactional
  public Long getTaskIdBySlug(String slug) {
    return taskCommentRepository.findDistinctTaskIdBySlug(slug);
  }

  @Override
  @Transactional
  public Long getTaskIdByTaskCommentId(Long taskCommentId) {
    return taskCommentRepository.findDistinctTaskIdById(taskCommentId);
  }
}
