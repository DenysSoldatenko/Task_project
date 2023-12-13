package com.example.taskmanagerproject.configurations.mail;

import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for storing mail properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {

  private String host;
  private int port;
  private String username;
  private String password;
  private Properties properties;
}
