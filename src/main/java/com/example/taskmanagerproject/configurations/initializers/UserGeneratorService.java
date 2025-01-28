package com.example.taskmanagerproject.configurations.initializers;

import static java.util.List.of;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.utils.factories.UserFactory;
import com.github.slugify.Slugify;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
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

  private final UserFactory userFactory;
  private final Slugify slugGenerator;
  private final UserRepository userRepository;

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
    return userRepository.save(buildUser("Alice Johnson", "alice12345@gmail.com", "password"));
  }

  private User createUser() {
    String fullName = faker.name().fullName();
    String username = generateUsername(fullName);
    String password = "password";
    return buildUser(fullName, username, password);
  }

  private User buildUser(String fullName, String username, String password) {
    return userFactory.createUserFromRequest(UserDto.builder()
      .fullName(fullName)
      .username(username)
      .slug(generateSlug(fullName))
      .password(password)
      .build());
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
}
