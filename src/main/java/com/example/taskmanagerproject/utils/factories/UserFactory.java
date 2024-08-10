package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND_WITH_NAME;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
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
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;

  /**
   * Creates a new User instance from a registration request.
   *
   * @param request The registration request containing user information.
   * @return A new User instance.
   */
  public User createUserFromRequest(final UserDto request) {
    Role role = getRoleFromRequest(request);
    return buildUserFromRequest(request, role);
  }

  /**
   * Retrieves the Role from the database based on the role name.
   *
   * @param request The registration request containing the role name.
   * @return The Role entity.
   */
  private Role getRoleFromRequest(final UserDto request) {
    return roleRepository.findByName(request.role().name())
      .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + request.role()));
  }

  /**
   * Builds the User entity from the registration request and the role.
   *
   * @param request The registration request containing user information.
   * @param role    The Role entity.
   * @return A new User instance.
   */
  private User buildUserFromRequest(final UserDto request, final Role role) {
    User user = new User();
    user.setFullName(request.fullName());
    user.setUsername(request.username());
    user.setSlug(generateSlugFromFullName(request.fullName()));
    user.setPassword(encodePassword(request.password()));
    user.setConfirmPassword(encodePassword(request.confirmPassword()));
    user.setRole(role);
    return user;
  }

  /**
   * Generates a unique slug from the user's full name by appending a shortened UUID.
   *
   * <p>The base slug is created from the full name,
   * and a unique 8-character suffix from a UUID is appended.
   *
   * @param fullName The full name to generate the slug for.
   * @return A unique slug consisting of the base slug and a shortened UUID.
   */
  private String generateSlugFromFullName(final String fullName) {
    String baseSlug = slugGenerator.slugify(fullName);
    String uniqueSuffix = randomUUID().toString().substring(0, 8);
    return baseSlug + "-" + uniqueSuffix;
  }

  /**
   * Encodes the password using the provided password encoder.
   *
   * @param password The plain-text password.
   * @return The encoded password.
   */
  private String encodePassword(final String password) {
    return passwordEncoder.encode(password);
  }
}
