package com.example.taskmanagerproject.utils;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    user.setUserRoles(Collections.singleton(Role.ROLE_USER));
    return user;
  }
}