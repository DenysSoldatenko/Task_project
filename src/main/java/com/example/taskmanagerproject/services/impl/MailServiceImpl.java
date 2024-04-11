package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.MailType;
import com.example.taskmanagerproject.services.MailService;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of the MailService interface for sending emails.
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

  private final Configuration configuration;
  private final JavaMailSender mailSender;

  @Override
  @SneakyThrows
  public void sendEmail(
      final UserDto user,
      final MailType type,
      final Properties params
  ) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
    helper.setTo(user.username());

    switch (type) {

      case REGISTRATION:
        prepareAndSendEmail(
            helper,
            "Welcome aboard, " + user.fullName(),
            "register.ftlh",
            user,
            params
        );
        break;

      case REMINDER:
        prepareAndSendEmail(
            helper,
            "Reminder: Task due in 1 hour",
            "reminder.ftlh",
            user,
            params
        );
        break;

      default: break;
    }
  }

  @SneakyThrows
  private void prepareAndSendEmail(
      final MimeMessageHelper helper,
      final String subject,
      final String templateName,
      final UserDto user,
      final Properties params
  ) {
    helper.setSubject(subject);
    String emailContent = getEmailContent(templateName, user, params);
    helper.setText(emailContent, true);
    mailSender.send(helper.getMimeMessage());
  }

  @SneakyThrows
  private String getEmailContent(
      final String templateName,
      final UserDto user,
      final Properties params
  ) {
    Map<String, Object> model = new HashMap<>();
    model.put("name", user.fullName());
    if (params.containsKey("task.title")) {
      model.put("title", params.getProperty("task.title"));
    }
    if (params.containsKey("task.description")) {
      model.put("description", params.getProperty("task.description"));
    }
    StringWriter writer = new StringWriter();
    configuration.getTemplate(templateName).process(model, writer);
    return writer.toString();
  }
}
