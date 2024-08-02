package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.RoleDto;
import com.example.taskmanagerproject.entities.Role;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Role entities to RoleDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

  /**
   * Converts a Role entity to a RoleDto object.
   *
   * @param role The Role entity to convert.
   * @return The corresponding RoleDto object.
   */
  RoleDto toDto(Role role);

  /**
   * Converts a RoleDto object to a Role entity.
   *
   * @param dto The RoleDto object to convert.
   * @return The corresponding Role entity.
   */
  Role toEntity(RoleDto dto);
}
