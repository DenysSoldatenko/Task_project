package com.example.taskmanagerproject.configurations.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuration class for setting up JavaMailSender.
 */
@Configuration
@RequiredArgsConstructor
public class MailConfig {

  private final MailProperties mailProperties;

  /**
   * Creates a JavaMailSender bean to send emails.
   *
   * @return A JavaMailSender instance.
   */
  @Bean
  public JavaMailSender mailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailProperties.getHost());
    mailSender.setPort(mailProperties.getPort());
    mailSender.setUsername(mailProperties.getUsername());
    mailSender.setPassword(mailProperties.getPassword());
    mailSender.setJavaMailProperties(mailProperties.getProperties());
    mailSender.getJavaMailProperties();
    return mailSender;
  }
}
