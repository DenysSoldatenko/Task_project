package com.example.taskmanagerproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The main class that starts the Task Manager application.
 */
@SpringBootApplication
@EnableTransactionManagement
public class TaskManagerProjectApplication {

  /**
   * The entry point for the Spring Boot application.
   *
   * @param args the command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(TaskManagerProjectApplication.class, args);
  }
}
