package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.entities.tasks.Task;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping Task entities to TaskDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class, TeamMapper.class, UserMapper.class})
public interface TaskMapper {

  /**
   * Converts a Task entity to a TaskDto.
   *
   * @param task the Task entity to convert
   * @return the corresponding TaskDto
   */
  TaskDto toDto(Task task);

  /**
   * Converts a TaskDto to a Task entity.
   *
   * @param dto the TaskDto to convert
   * @return the corresponding Task entity
   */
  Task toEntity(TaskDto dto);
}
