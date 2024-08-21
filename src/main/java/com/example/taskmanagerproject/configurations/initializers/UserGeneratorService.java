package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_NOT_FOUND_WITH_NAME;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.security.Role;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.exceptions.RoleNotFoundException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.github.slugify.Slugify;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for generating user-related data.
 */
@Service
@RequiredArgsConstructor
public class UserGeneratorService {

  private final Faker faker = new Faker();

  private final Slugify slugGenerator;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Generates a batch of users with random data.
   *
   * @param batchSize the number of users to generate
   * @return a list of users
   */
  public List<User> generateUserBatch(int batchSize) {
    return userRepository.saveAll(range(0, batchSize).mapToObj(i -> createUser()).toList());
  }

  /**
   * Creates a global admin user with predefined attributes.
   *
   * @return the created global admin user
   */
  public User createGlobalAdminUser() {
    User globalAdminUser = new User();
    globalAdminUser.setFullName("Alice Johnson");
    globalAdminUser.setUsername("alice12345@gmail.com");
    globalAdminUser.setSlug(generateSlugFromFullName("Alice Johnson"));
    globalAdminUser.setPassword(passwordEncoder.encode("password123"));
    globalAdminUser.setConfirmPassword(passwordEncoder.encode("password123"));
    globalAdminUser.setRole(getAdminRole());
    return userRepository.save(globalAdminUser);
  }

  private User createUser() {
    String fullName = faker.name().fullName();
    User user = new User();
    user.setFullName(fullName);
    user.setUsername(faker.internet().emailAddress());
    user.setSlug(generateSlugFromFullName(fullName));
    user.setPassword(passwordEncoder.encode("password123"));
    user.setConfirmPassword(passwordEncoder.encode("password123"));
    user.setRole(getRandomRole());
    return user;
  }

  private String generateSlugFromFullName(String fullName) {
    String baseSlug = slugGenerator.slugify(fullName);
    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    return baseSlug + "-" + uniqueSuffix;
  }

  private Role getRandomRole() {
    return roleRepository.findAll().get(faker.number().numberBetween(0, roleRepository.findAll().size()));
  }

  private Role getAdminRole() {
    return roleRepository.findAll().stream()
      .filter(role -> "ADMIN".equals(role.getName()))
      .findFirst()
      .orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_WITH_NAME + "ADMIN"));
  }
}
