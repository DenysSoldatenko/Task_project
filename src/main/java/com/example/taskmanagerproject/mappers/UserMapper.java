package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.RoleName;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static com.example.taskmanagerproject.entities.RoleName.USER;

/**
 * Mapper interface for converting User entities to UserDto objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
  UserDto toDto(User user);

  @Mapping(target = "userTeams", ignore = true)
  @Mapping(target = "userTasks", ignore = true)
  @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
  User toEntity(UserDto dto);

  @Named("roleToString")
  default String roleToString(Role role) {
    return role != null ? role.getName().toString() : null;
  }

  @Named("stringToRole")
  default Role stringToRole(String roleName) {
    if (roleName != null) {
      return Role.builder().name(RoleName.valueOf(roleName)).build();
    }
    return Role.builder().name(USER).build(); // Default to USER
  }
}
