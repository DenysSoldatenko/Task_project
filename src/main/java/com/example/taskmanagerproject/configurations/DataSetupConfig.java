package com.example.taskmanagerproject.configurations;

import static com.example.taskmanagerproject.entities.TaskStatus.values;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.Task;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for setting up initial data in the application.
 */
//@Configuration
@RequiredArgsConstructor
public class DataSetupConfig {

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public CommandLineRunner setUpData() {
    return args -> generateUsers();
  }

  private void generateUsers() {
    Faker faker = new Faker();
    for (int i = 0; i < 100; i++) {
      User user = createUser(faker);
      userRepository.save(user);
      generateTasksForUser(user, faker);
    }
  }

  private User createUser(Faker faker) {
    User user = new User();
    user.setFullName(faker.name().fullName());
    user.setUsername(faker.internet().emailAddress());
    user.setPassword(passwordEncoder.encode("1234567"));
    user.setConfirmPassword(passwordEncoder.encode("1234567"));
    user.setUserRoles(Collections.singleton(Role.ROLE_USER));
    return user;
  }

  private void generateTasksForUser(User user, Faker faker) {
    int numTasks = faker.number().numberBetween(1, 10);
    for (int j = 0; j < numTasks; j++) {
      Task task = createTask(faker);
      taskRepository.save(task);
      taskRepository.assignTaskToUser(user.getId(), task.getId());
    }
  }

  private Task createTask(Faker faker) {
    Task task = new Task();
    task.setTitle(faker.lorem().sentence());
    task.setDescription(faker.lorem().sentence());
    task.setTaskStatus(values()[faker.number().numberBetween(0, values().length)]);
    task.setExpirationDate(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 30)));
    return task;
  }
}
