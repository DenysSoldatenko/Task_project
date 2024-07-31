package com.example.taskmanagerproject.configurations.data;

import static com.example.taskmanagerproject.entities.TaskStatus.values;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singleton;
import static java.util.stream.IntStream.range;

import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Component responsible for generating and inserting initial user and task data into the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataGenerator {

  private static final int BATCH_SIZE = 10;
  private static final int NUM_USERS = 100;
  private static final int MIN_TASKS_PER_USER = 1;
  private static final int MAX_TASKS_PER_USER = 10;
  private static final int MAX_DAYS_TO_EXPIRE = 30;

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Generates and inserts user data into the database.
   *
   * <p>This method creates users in batches and generates tasks for each user.
   * The data is inserted into the {@link UserRepository} and {@link TaskRepository}.
   */
  public void generateUsers() {
    Faker faker = new Faker();

    range(0, NUM_USERS / BATCH_SIZE)
        .parallel()
        .forEach(batchIndex -> {
          try {
            List<User> userBatch = createUserBatch(faker);
            userRepository.saveAll(userBatch);
            userBatch.forEach(user -> generateTasksForUser(user, faker));
            log.info("User batch {} inserted successfully.", batchIndex);
          } catch (Exception e) {
            log.error("Error in user batch {}: {}", batchIndex, e.getMessage(), e);
          }
        });
  }

  private List<User> createUserBatch(Faker faker) {
    return range(0, BATCH_SIZE).mapToObj(i -> createUser(faker)).toList();
  }

  private User createUser(final Faker faker) {
    User user = new User();
    user.setFullName(faker.name().fullName());
    user.setUsername(faker.internet().emailAddress());
    user.setPassword(passwordEncoder.encode("password123"));
    user.setConfirmPassword(passwordEncoder.encode("password123"));
    return user;
  }

  private void generateTasksForUser(final User user, final Faker faker) {
    int numTasks = faker.number().numberBetween(MIN_TASKS_PER_USER, MAX_TASKS_PER_USER);
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
    return task;
  }
}
