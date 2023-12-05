package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.entities.TaskImage;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping User entities to TaskImageDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface TaskImageMapper {

  TaskImageDto toDto(TaskImage user);

  TaskImage toEntity(TaskImageDto dto);
}