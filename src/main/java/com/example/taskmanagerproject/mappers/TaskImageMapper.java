package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.TaskImageDto;
import com.example.taskmanagerproject.entities.TaskImage;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping User entities
 * to TaskImageDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface TaskImageMapper {

  /**
   * Converts a TaskImage entity to a TaskImageDto object.
   *
   * @param taskImage The TaskImage entity to convert.
   * @return The corresponding TaskImageDto object.
   */
  TaskImageDto toDto(TaskImage taskImage);

  /**
   * Converts a TaskImageDto object to a TaskImage entity.
   *
   * @param dto The TaskImageDto object to convert.
   * @return The corresponding TaskImage entity.
   */
  TaskImage toEntity(TaskImageDto dto);
}
