package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.entities.task.Task;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping Task entities to TaskDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

  TaskDto toDto(Task task);

  Task toEntity(TaskDto dto);
}
