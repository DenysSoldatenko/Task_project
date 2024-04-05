package com.example.taskmanagerproject.configurations.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up initial data in the application.
 *
 * <p>Note: To properly insert data, you need to uncomment {@code @Transactional} in
 * {@code TaskRepository} in the method {@code void assignTaskToUser(@Param("userId") Long userId,
 * @Param("taskId") Long taskId)}.
 */
@Slf4j
//@Configuration
@RequiredArgsConstructor
public class DataSetupConfig {

  private final DataGenerator dataGenerator;

  /**
   * Sets up initial data for the application.
   *
   * @return A CommandLineRunner for setting up initial data.
   */
  @Bean
  public CommandLineRunner setUpData() {
    return args -> {
      log.info("Setting up initial data...");
      dataGenerator.generateUsers();
      log.info("Initial data setup completed");
    };
  }
}
