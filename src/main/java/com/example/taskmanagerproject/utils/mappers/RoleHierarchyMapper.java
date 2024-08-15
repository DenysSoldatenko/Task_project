package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.security.RoleHierarchyDto;
import com.example.taskmanagerproject.entities.security.RoleHierarchy;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Role entities to RoleHierarchyDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface RoleHierarchyMapper {

  /**
   * Converts a RoleHierarchy entity to a RoleDto object.
   *
   * @param roleHierarchy The Role entity to convert.
   * @return The corresponding RoleDto object.
   */
  RoleHierarchyDto toDto(RoleHierarchy roleHierarchy);

  /**
   * Converts a RoleHierarchyDto object to a Role entity.
   *
   * @param dto The RoleDto object to convert.
   * @return The corresponding Role entity.
   */
  RoleHierarchy toEntity(RoleHierarchyDto dto);
}