package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.entities.Role.ROLE_USER;
import static java.util.Collections.singleton;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Factory class for creating User instances from registration requests.
 */
@Component
@RequiredArgsConstructor
public class UserFactory {

  private final PasswordEncoder passwordEncoder;

  /**
   * Creates a new User instance from a registration request.
   *
   * @param request The registration request containing user information.
   * @return A new User instance.
   */
  public User createUserFromRequest(UserDto request) {
    User user = new User();
    user.setFullName(request.fullName());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setConfirmPassword(passwordEncoder.encode(request.confirmPassword()));
    user.setUserRoles(Collections.singleton(ROLE_USER)); // Assuming ROLE_USER is defined elsewhere
    return user;
  }
}