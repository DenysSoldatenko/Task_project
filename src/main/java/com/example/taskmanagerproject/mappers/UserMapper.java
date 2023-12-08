package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for mapping User entities to UserDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toDto(User user);

  @Mapping(target = "userRoles", ignore = true)
  @Mapping(target = "userTasks", ignore = true)
  User toEntity(UserDto dto);
}
