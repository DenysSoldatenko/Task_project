package com.example.taskmanagerproject.utils.mappers;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import org.mapstruct.Mapper;

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
  UserDto toDto(User user);

  /**
   * Converts a UserDto object to a User entity.
   *
   * @param dto The UserDto object to convert.
   * @return The corresponding User entity.
   */
  User toEntity(UserDto dto);
}
