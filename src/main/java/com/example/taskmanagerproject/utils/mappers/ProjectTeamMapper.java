package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting ProjectTeam entities to ProjectTeamDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class, TeamMapper.class})
public interface ProjectTeamMapper {

  /**
   * Converts a ProjectTeam entity to a ProjectTeamDto object.
   *
   * @param projectTeam The ProjectTeam entity to convert.
   * @return The corresponding ProjectTeamDto object.
   */
  @Mapping(source = "projectTeam.team", target = "team")
  @Mapping(source = "projectTeam.project", target = "project")
  ProjectTeamDto toDto(ProjectTeam projectTeam);

  /**
   * Converts a ProjectTeamDto object to a ProjectTeam entity.
   *
   * @param dto The ProjectTeamDto object to convert.
   * @return The corresponding ProjectTeam entity.
   */
  @Mapping(source = "team", target = "team")
  @Mapping(source = "project", target = "project")
  ProjectTeam toEntity(ProjectTeamDto dto);
}
