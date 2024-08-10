package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.ProjectDto;
import com.example.taskmanagerproject.entities.Project;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Project entities to ProjectDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

  /**
   * Converts a Project entity to a ProjectDto object.
   *
   * @param project The Project entity to convert.
   * @return The corresponding ProjectDto object.
   */
  ProjectDto toDto(Project project);

  /**
   * Converts a ProjectDto object to a Project entity.
   *
   * @param dto The ProjectDto object to convert.
   * @return The corresponding Project entity.
   */
  Project toEntity(ProjectDto dto);
}
