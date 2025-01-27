package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting User entities to UserDto objects and vice versa.
 */
@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {

  /**
   * Converts a User entity to a UserDto object.
   *
   * @param user The User entity to convert.
   * @return The corresponding UserDto object.
   */
  @Mapping(target = "password", ignore = true)
  UserDto toDto(User user);

  /**
   * Converts a UserDto object to a User entity.
   *
   * @param dto The UserDto object to convert.
   * @return The corresponding User entity.
   */
  User toEntity(UserDto dto);
}
