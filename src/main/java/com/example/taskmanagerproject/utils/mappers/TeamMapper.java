package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.entities.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting Team entities to TeamDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = UserTeamMapper.class)
public interface TeamMapper {

  /**
   * Converts a Team entity to a TeamDto object.
   *
   * @param team The Team entity to convert.
   * @return The corresponding TeamDto object.
   */
  @Mapping(source = "userTeams", target = "userTeams")
  TeamDto toDto(Team team);

  /**
   * Converts a TeamDto object to a Team entity.
   *
   * @param dto The TeamDto object to convert.
   * @return The corresponding Team entity.
   */
  Team toEntity(TeamDto dto);
}
