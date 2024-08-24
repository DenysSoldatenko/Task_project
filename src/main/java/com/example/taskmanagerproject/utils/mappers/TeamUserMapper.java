package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting UserTeam entities to UserTeamDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, TeamMapper.class, RoleMapper.class})
public interface TeamUserMapper {

  /**
   * Converts a UserTeam entity to a UserTeamDto object.
   *
   * @param teamUser The UserTeam entity to convert.
   * @return The corresponding UserTeamDto object.
   */
  @Mapping(source = "teamUser.user", target = "user")
  @Mapping(source = "teamUser.team", target = "team")
  @Mapping(source = "teamUser.role", target = "role")
  TeamUserDto toDto(TeamUser teamUser);

  /**
   * Converts a UserTeamDto object to a UserTeam entity.
   *
   * @param dto The UserTeamDto object to convert.
   * @return The corresponding UserTeam entity.
   */
  @Mapping(source = "user", target = "user")
  @Mapping(source = "team", target = "team")
  @Mapping(source = "role", target = "role")
  TeamUser toEntity(TeamUserDto dto);
}
