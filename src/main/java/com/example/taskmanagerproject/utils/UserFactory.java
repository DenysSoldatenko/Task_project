package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.entities.Role.ROLE_USER;
import static java.util.Collections.singleton;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
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
    return User.builder()
    .fullName(request.fullName())
    .username(request.username())
    .password(passwordEncoder.encode(request.password()))
    .confirmPassword(passwordEncoder.encode(request.confirmPassword()))
    .userRoles(singleton(ROLE_USER))
    .build();
  }
}