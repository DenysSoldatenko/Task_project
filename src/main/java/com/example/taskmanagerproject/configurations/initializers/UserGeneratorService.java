package com.example.taskmanagerproject.configurations.initializers;

import static java.util.List.of;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.github.slugify.Slugify;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Generates user-related data.
 */
@Service
@RequiredArgsConstructor
public class UserGeneratorService {

  private static final List<String> EMAIL_DOMAINS = of("@yahoo.com", "@hotmail.com", "@gmail.com");
  private static final List<String> COMMON_TITLES = of("dr.", "mr.", "mrs.", "ms.", "miss", "prof.");
  private static final Random RANDOM = new Random();
  private final Faker faker = new Faker();

  private final Slugify slugGenerator;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Generates a batch of users.
   *
   * @param batchSize the number of users to generate
   * @return a list of users
   */
  public List<User> generateUserBatch(int batchSize) {
    return userRepository.saveAll(range(0, batchSize).mapToObj(i -> createUser()).toList());
  }

  /**
   * Creates a global admin user.
   *
   * @return the created global admin user
   */
  public User createGlobalAdminUser() {
    return userRepository.save(buildUser("Alice Johnson", "alice12345@gmail.com", getRandomRole()));
  }

  private User createUser() {
    String fullName = faker.name().fullName();
    return buildUser(fullName, generateUsername(fullName), getRandomRole());
  }

  private User buildUser(String fullName, String username, Role role) {
    return User.builder()
      .fullName(fullName)
      .username(username)
      .slug(generateSlug(fullName))
      .password(hashPassword())
      .confirmPassword(hashPassword())
      .role(role)
      .build();
  }

  private String generateUsername(String fullName) {
    String[] nameParts = removeTitle(fullName).split("\\s+");
    return nameParts[0].toLowerCase() + "." + nameParts[1].toLowerCase() + getRandomEmailDomain();
  }

  private String removeTitle(String fullName) {
    return fullName.replaceAll("(?i)^(" + String.join("|", COMMON_TITLES) + ")\\s+", "").trim();
  }

  private String getRandomEmailDomain() {
    return EMAIL_DOMAINS.get(RANDOM.nextInt(EMAIL_DOMAINS.size()));
  }

  private String generateSlug(String fullName) {
    return slugGenerator.slugify(fullName) + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private Role getRandomRole() {
    List<Role> roles = roleRepository.findAll();
    return roles.isEmpty() ? null : roles.get(RANDOM.nextInt(roles.size()));
  }

  private String hashPassword() {
    return passwordEncoder.encode("password123");
  }
}
