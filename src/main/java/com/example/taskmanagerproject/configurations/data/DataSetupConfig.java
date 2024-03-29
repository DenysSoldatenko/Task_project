package com.example.taskmanagerproject.configurations.data;

import static com.example.taskmanagerproject.entities.TaskStatus.values;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for setting up initial data in the application.
 */
//@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataSetupConfig {

  private static final int NUM_USERS = 100;
  private static final int MIN_TASKS_PER_USER = 1;
  private static final int MAX_TASKS_PER_USER = 10;
  private static final int MAX_DAYS_TO_EXPIRE = 30;

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Sets up initial data for the application.
   *
   * @return A CommandLineRunner for setting up initial data.
   */
  @Bean
  public CommandLineRunner setUpData() {
    return args -> {
      log.info("Setting up initial data...");
      generateUsers();
      log.info("Initial data setup completed");
    };
  }

  private void generateUsers() {
    Faker faker = new Faker();
    for (int i = 0; i < NUM_USERS; i++) {
      User user = createUser(faker);
      userRepository.save(user);
      generateTasksForUser(user, faker);
    }
  }

  private User createUser(final Faker faker) {
    User user = new User();
    user.setFullName(faker.name().fullName());
    user.setUsername(faker.internet().emailAddress());
    user.setPassword(passwordEncoder.encode("1234567"));
    user.setConfirmPassword(passwordEncoder.encode("1234567"));
    user.setUserRoles(Collections.singleton(Role.ROLE_USER));
    return user;
  }

  private void generateTasksForUser(final User user, final Faker faker) {
    int numTasks = faker.number()
        .numberBetween(MIN_TASKS_PER_USER, MAX_TASKS_PER_USER);
    for (int j = 0; j < numTasks; j++) {
      Task task = createTask(faker);
      taskRepository.save(task);
      taskRepository.assignTaskToUser(user.getId(), task.getId());
    }
  }

  private Task createTask(final Faker faker) {
    Task task = new Task();
    task.setTitle(faker.lorem().sentence());
    task.setDescription(faker.lorem().sentence());
    task.setTaskStatus(
        values()[faker.number().numberBetween(0, values().length)]
    );
    task.setExpirationDate(
        LocalDateTime.now().plusDays(
          faker.number().numberBetween(1, MAX_DAYS_TO_EXPIRE)
        )
    );
    return task;
  }
}
