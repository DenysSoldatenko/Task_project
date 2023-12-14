package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.MailType;
import java.util.Properties;

/**
 * Interface for sending emails.
 */
public interface MailService {

  void sendEmail(UserDto user, MailType type, Properties params);
}
