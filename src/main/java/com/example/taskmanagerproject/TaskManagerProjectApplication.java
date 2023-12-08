package com.example.taskmanagerproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The main class that starts the Task Manager application.
 */
//@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
public class TaskManagerProjectApplication {

  public static void main(final String[] args) {
    SpringApplication.run(TaskManagerProjectApplication.class, args);
  }

}
