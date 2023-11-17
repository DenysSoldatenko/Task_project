package com.example.taskmanagerproject.dtos;

import lombok.ToString;

/**
 * Represents a user DTO (Data Transfer Object) in the project.
 */
public record UserDto(Long id, String fullName, String username,
                      @ToString.Include(name = "password") String password,
                      @ToString.Include(name = "confirmPassword") String confirmPassword) {
}
