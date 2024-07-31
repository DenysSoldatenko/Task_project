package com.example.taskmanagerproject.utils;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.RoleName;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.RoleRepository;
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
  private final RoleRepository roleRepository;

  /**
   * Creates a new User instance from a registration request.
   *
   * @param request The registration request containing user information.
   * @return A new User instance.
   */
  public User createUserFromRequest(final UserDto request) {
    Role role = roleRepository.findByName(RoleName.valueOf(request.role()))
      .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND));

    User user = new User();
    user.setFullName(request.fullName());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setConfirmPassword(passwordEncoder.encode(request.confirmPassword()));
    user.setRole(role);
    return user;
  }
}
