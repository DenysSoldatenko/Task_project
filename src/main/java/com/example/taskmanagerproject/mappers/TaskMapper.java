package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.entities.Task;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping Task entities to TaskDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

  TaskDto toDto(Task task);

  Task toEntity(TaskDto dto);
}