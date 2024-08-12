package com.example.taskmanagerproject.configurations.initializers;

import static com.example.taskmanagerproject.entities.RoleName.ADMIN;
import static com.example.taskmanagerproject.entities.RoleName.MANAGER;
import static com.example.taskmanagerproject.entities.RoleName.PRODUCT_OWNER;
import static com.example.taskmanagerproject.entities.RoleName.SCRUM_MASTER;
import static com.example.taskmanagerproject.entities.RoleName.TEAM_LEAD;
import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_FAIL_MESSAGE;
import static com.example.taskmanagerproject.utils.MessageUtils.DATA_INITIALIZATION_SUCCESS_MESSAGE;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.Project;
import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.github.slugify.Slugify;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration class for initializing role, user, and project data in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

  private static final List<String> PROJECT_CREATION_ALLOWED_ROLES = asList(
      ADMIN.name(), PRODUCT_OWNER.name(), SCRUM_MASTER.name(), MANAGER.name(), TEAM_LEAD.name()
  );

  private final Slugify slugGenerator;
  private final PasswordEncoder passwordEncoder;
  private final Faker faker = new Faker();

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;

  /**
   * Initializes role, user, and project data in the database.
   */
  @Transactional
  public String initData() {
    int userBatchSize = 10;
    int totalUsers = 100;
    int totalProjectsPerUser = 3;
    AtomicBoolean hasErrors = new AtomicBoolean(false);

    log.info("Starting data initialization...");

    try {
      range(0, totalUsers / userBatchSize)
          .parallel()
          .forEach(batchIndex -> {
            try {
              List<User> userBatch = generateUserBatch(userBatchSize);
              userRepository.saveAll(userBatch);
              log.info("User batch {} inserted successfully.", batchIndex);
              userBatch.forEach(user -> generateProjectsForUser(user, totalProjectsPerUser));
            } catch (Exception e) {
              log.error("Error in user batch {}: {}", batchIndex, e.getMessage(), e);
              hasErrors.set(true);
            }
          });

    } catch (Exception e) {
      log.error("Error during data initialization: {}", e.getMessage(), e);
      hasErrors.set(true);
    }

    if (hasErrors.get()) {
      log.warn("Data initialization completed with errors.");
      return DATA_INITIALIZATION_FAIL_MESSAGE;
    } else {
      log.info("Data initialization completed successfully.");
      return DATA_INITIALIZATION_SUCCESS_MESSAGE;
    }
  }

  /**
   * Generates a batch of users with random data.
   *
   * @param batchSize the number of users to generate
   * @return a list of users
   */
  private List<User> generateUserBatch(int batchSize) {
    return IntStream.range(0, batchSize)
      .mapToObj(i -> createUser())
      .toList();
  }

  /**
   * Creates a single user with random data.
   *
   * @return a User object
   */
  private User createUser() {
    User user = new User();
    String fullName = faker.name().fullName();
    user.setFullName(fullName);
    user.setUsername(faker.internet().emailAddress());
    user.setSlug(generateSlugFromFullName(fullName));
    user.setPassword(passwordEncoder.encode("password123"));
    user.setConfirmPassword(passwordEncoder.encode("password123"));
    user.setRole(getRandomRole());
    return user;
  }

  /**
   * Generates a slug from a user's full name.
   *
   * @param fullName the full name of the user
   * @return the generated slug
   */
  private String generateSlugFromFullName(final String fullName) {
    String baseSlug = slugGenerator.slugify(fullName);
    String uniqueSuffix = randomUUID().toString().substring(0, 8);
    return baseSlug + "-" + uniqueSuffix;
  }

  /**
   * Retrieves a random role from the role repository.
   *
   * @return a random Role
   */
  private Role getRandomRole() {
    List<Role> roles = roleRepository.findAll();
    return roles.get(faker.number().numberBetween(0, roles.size()));
  }

  /**
   * Generates projects for a user, ensuring that only users with allowed roles can create projects.
   *
   * @param user the user for whom projects are being created
   * @param totalProjects the number of projects to create
   */
  private void generateProjectsForUser(User user, int totalProjects) {
    if (!isUserAllowedToCreateProjects(user.getRole())) {
      //log.info("User {} is not authorized to create projects.", user.getUsername());
      return;
    }

    range(0, totalProjects)
        .forEach(i -> {
          Project project = createProject(user);
          projectRepository.save(project);
          //log.info("Created project: {} for user: {}", project.getName(), user.getUsername());
        });
  }

  /**
   * Checks if the user has a role that allows them to create projects.
   *
   * @param userRole the role of the user
   * @return true if the user's role allows project creation, false otherwise
   */
  private boolean isUserAllowedToCreateProjects(Role userRole) {
    return PROJECT_CREATION_ALLOWED_ROLES.contains(userRole.getName());
  }

  /**
   * Creates a project associated with a user.
   *
   * @param user the creator of the project
   * @return the created Project object
   */
  private Project createProject(User user) {
    Project project = new Project();
    project.setName(faker.company().name());
    project.setDescription(faker.lorem().sentence());
    project.setCreator(user);
    project.setCreatedAt(generateRandomDateTime());
    return project;
  }

  /**
   * Generates a random LocalDateTime within the last 12 months.
   */
  private LocalDateTime generateRandomDateTime() {
    long daysAgo = current().nextLong(0, 365);
    return now().minusDays(daysAgo);
  }
}
