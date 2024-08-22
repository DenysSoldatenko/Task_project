package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.generatePdfFromHtml;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.loadTemplate;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating PDF reports from HTML templates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfReportFactory {

  private static final String USER_TEMPLATE_PATH = "src/main/resources/report_templates/user_template.html";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TaskRepository taskRepository;
  private final TeamUserRepository teamUserRepository;

  public byte[] generateReport(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    String htmlTemplate = loadTemplate(USER_TEMPLATE_PATH);

    if (htmlTemplate.isEmpty()) {
      throw new PdfGenerationException("Failed to load the HTML template from " + USER_TEMPLATE_PATH);
    }

    String userName = user.getFullName();
    String teamName = team.getName();
    String projectName = project.getName();
    String teamObjective = team.getDescription();
    String role = teamUserRepository.findRoleByTeamNameAndUsername(teamName, user.getUsername()).getName();
    role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

    String allTasks = taskRepository.countAllTasksForUser(user.getId(), startDate, endDate, projectName, teamName).toString();
    String tasksCompleted = taskRepository.countCompletedTasksForUser(user.getId(), startDate, endDate, projectName, teamName).toString();
    String taskCompletionRate = taskRepository.calculateTaskCompletionRateForUser(user.getId(), startDate, endDate, projectName, teamName).toString();
    String onTimeTasks = taskRepository.countCompletedOnTimeTasks(user.getId(), startDate, endDate, projectName, teamName).toString();
    String allBugs = taskRepository.countTasksWithCommentsByUser(user.getId(), false, false, startDate, endDate, projectName, teamName).toString();
    String bugFixesResolved = taskRepository.countTasksWithCommentsByUser(user.getId(), true, true, startDate, endDate, projectName, teamName).toString();
    String allCriticalTasks = taskRepository.countCriticalTasks(user.getId(), false, startDate, endDate, projectName, teamName).toString();
    String criticalTasksSolved = taskRepository.countCriticalTasks(user.getId(), true, startDate, endDate, projectName, teamName).toString();
    String averageTaskDuration = convertMinutesToHoursMinutes(taskRepository.calculateAverageMinutesSpent(user.getId(), startDate, endDate, project.getId(), team.getId()));

    String populatedHtml = htmlTemplate
      .replace("{startDate}", startDate.format(DATE_TIME_FORMATTER))
      .replace("{endDate}", endDate.format(DATE_TIME_FORMATTER))
      .replace("{username}", userName)
      .replace("{role}", role)
      .replace("{teamName}", teamName)
      .replace("{teamObjective}", teamObjective)
      .replace("{tasksCompleted}", tasksCompleted + "/" + allTasks)
      .replace("{taskCompletionRate}", taskCompletionRate)
      .replace("{onTimeTasks}", onTimeTasks + "/" + tasksCompleted)
      .replace("{averageTaskDuration}", averageTaskDuration)
      .replace("{bugFixesResolved}", bugFixesResolved + "/" + allBugs)
      .replace("{criticalTasksSolved}", criticalTasksSolved + "/" + allCriticalTasks);

    return generatePdfFromHtml(populatedHtml);
  }

  private String convertMinutesToHoursMinutes(double averageTaskDuration) {
    double hours = averageTaskDuration / 60.0;
    double remainingMinutes = averageTaskDuration % 60;
    return String.format(Locale.US, "%.1f hours, %.1f minutes", hours, remainingMinutes);
  }
}
