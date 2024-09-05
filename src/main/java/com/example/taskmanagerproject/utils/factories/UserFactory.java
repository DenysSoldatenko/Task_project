package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND_WITH_NAME;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating User instances from registration requests.
 */
@Component
@RequiredArgsConstructor
public final class UserFactory {

  private final Slugify slugGenerator;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates a new User instance from a registration request.
   *
   * @param request The registration request containing user information.
   * @return A new User instance.
   */
  public User createUserFromRequest(UserDto request) {
    Role role = roleRepository.findByName("USER")
        .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_NAME + "USER"));

    return User.builder()
      .fullName(request.fullName())
      .username(request.username())
      .slug(generateSlug(request.fullName()))
      .password(encodePassword(request.password()))
      .confirmPassword(encodePassword(request.confirmPassword()))
      .role(role)
      .build();
  }

  /**
   * Generates a unique slug from the user's full name.
   *
   * @param fullName The full name to generate the slug for.
   * @return A unique slug consisting of the base slug and a shortened UUID.
   */
  private String generateSlug(String fullName) {
    return slugGenerator.slugify(fullName) + "-" + randomUUID().toString().substring(0, 8);
  }

  /**
   * Encodes the password using the provided password encoder.
   *
   * @param password The plain-text password.
   * @return The encoded password.
   */
  private String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }
}
