package com.example.taskmanagerproject.mappers;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserDto toDto(User user);

  User toEntity(UserDto dto);
}