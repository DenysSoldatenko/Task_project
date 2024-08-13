package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.UserTeamDto;
import com.example.taskmanagerproject.entities.UserTeam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting UserTeam entities to UserTeamDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, TeamMapper.class, RoleMapper.class})
public interface UserTeamMapper {

  /**
   * Converts a UserTeam entity to a UserTeamDto object.
   *
   * @param userTeam The UserTeam entity to convert.
   * @return The corresponding UserTeamDto object.
   */
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "team.id", target = "teamId")
  @Mapping(source = "role.id", target = "roleId")
  UserTeamDto toDto(UserTeam userTeam);

  /**
   * Converts a UserTeamDto object to a UserTeam entity.
   *
   * @param dto The UserTeamDto object to convert.
   * @return The corresponding UserTeam entity.
   */
  @Mapping(source = "userId", target = "user.id")
  @Mapping(source = "teamId", target = "team.id")
  @Mapping(source = "roleId", target = "role.id")
  UserTeam toEntity(UserTeamDto dto);
}
