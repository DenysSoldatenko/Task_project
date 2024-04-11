package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.MailType.REMINDER;
import static java.time.Duration.ofHours;

import com.example.taskmanagerproject.dtos.TaskDto;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.services.MailService;
import com.example.taskmanagerproject.services.TaskReminderService;
import com.example.taskmanagerproject.services.TaskService;
import com.example.taskmanagerproject.services.UserService;
import java.time.Duration;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TaskReminderService interface
 * for sending task reminders.
 */
@Service
@RequiredArgsConstructor
public class TaskReminderServiceImpl implements TaskReminderService {

  private static final Duration DURATION = ofHours(1);

  private final TaskService taskService;
  private final UserService userService;
  private final MailService mailService;

  @Override
  @Scheduled(cron = "0 * * * * *")
  public void remindForTask() {
    taskService.findAllSoonExpiringTasks(DURATION)
        .forEach(task -> {
            UserDto user = userService.getTaskAuthor(task.id());
            mailService.sendEmail(user, REMINDER, createPropertiesForTask(task));
          }
        );
  }

  private Properties createPropertiesForTask(final TaskDto task) {
    Properties properties = new Properties();
    properties.setProperty("task.title", task.title());
    properties.setProperty("task.description", task.description());
    return properties;
  }
}
