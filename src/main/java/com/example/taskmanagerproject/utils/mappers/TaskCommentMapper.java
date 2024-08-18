package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.task.TaskCommentDto;
import com.example.taskmanagerproject.entities.task.TaskComment;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping TaskComment entities to TaskCommentDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = {TaskMapper.class, UserMapper.class})
public interface TaskCommentMapper {

  /**
   * Converts a TaskComment entity to a TaskCommentDto.
   *
   * @param taskComment the TaskComment entity to convert
   * @return the corresponding TaskCommentDto
   */
  TaskCommentDto toDto(TaskComment taskComment);

  /**
   * Converts a TaskCommentDto to a TaskComment entity.
   *
   * @param dto the TaskCommentDto to convert
   * @return the corresponding TaskComment entity
   */
  TaskComment toEntity(TaskCommentDto dto);
}
