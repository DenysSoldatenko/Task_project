package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.generatePdfFromHtml;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.loadTemplate;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.example.taskmanagerproject.repositories.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
  private final AchievementRepository achievementRepository;

  public byte[] generateReport(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    String htmlTemplate = loadTemplate(USER_TEMPLATE_PATH);
    String role = teamUserRepository.findRoleByTeamNameAndUsername(team.getName(), user.getUsername()).getName();
    role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

    List<Object[]> taskMetricsList = taskRepository.getTaskMetricsByAssignedUser(user.getId(), startDate, endDate, project.getName(), team.getName());

    if (taskMetricsList.isEmpty()) {
      throw new PdfGenerationException("No task metrics found for user " + user.getUsername() + " in project " + project.getName());
    }

    Object[] taskMetrics = taskMetricsList.get(0);
    String allTasks = taskMetrics[2].toString(); // allTasks
    String tasksCompleted = taskMetrics[3].toString(); // tasksCompleted
    String taskCompletionRate = taskMetrics[4].toString(); // taskCompletionRate
    String onTimeTasks = taskMetrics[5].toString(); // onTimeTasks
    String allBugs = taskMetrics[6].toString(); // allBugs
    String bugFixesResolved = taskMetrics[7].toString(); // bugFixesResolved
    String allCriticalTasks = taskMetrics[8].toString(); // allCriticalTasks
    String criticalTasksSolved = taskMetrics[9].toString(); // criticalTasksSolved
    String averageTaskDuration = convertMinutesToHoursMinutes(Double.parseDouble(taskMetrics[10].toString())); // averageTaskDuration
    List<Achievement> achievementList = achievementRepository.findAchievementsByUserTeamAndProject(user.getId(), team.getId(), project.getId());

    String populatedHtml = htmlTemplate
      .replace("{startDate}", startDate.format(DATE_TIME_FORMATTER))
      .replace("{endDate}", endDate.format(DATE_TIME_FORMATTER))
      .replace("{fullName}", user.getFullName())
      .replace("{email}", user.getUsername())
      .replace("{role}", role)
      .replace("{teamName}", team.getName())
      .replace("{teamObjective}", team.getDescription())
      .replace("{projectName}", project.getName())
      .replace("{projectDescription}", project.getDescription())
      .replace("{tasksCompleted}", tasksCompleted + "/" + allTasks)
      .replace("{taskCompletionRate}", taskCompletionRate)
      .replace("{onTimeTasks}", onTimeTasks + "/" + tasksCompleted)
      .replace("{averageTaskDuration}", averageTaskDuration)
      .replace("{bugFixesResolved}", bugFixesResolved + "/" + allBugs)
      .replace("{criticalTasksSolved}", criticalTasksSolved + "/" + allCriticalTasks)
      .replace("{achievements}", generateAchievementsHtml(achievementList));

    return generatePdfFromHtml(populatedHtml);
  }

  private String generateAchievementsHtml(List<Achievement> achievements) {
    if (achievements.isEmpty()) {
      return "<div class=\"no-achievements\">There are no achievements yet.</div>";
    }

    StringBuilder achievementsHtml = new StringBuilder();
    for (Achievement achievement : achievements) {
      achievementsHtml.append("<div class=\"achievement\">")
        .append("<img src=\"").append(achievement.getImageUrl()).append("\" alt=\"Achievement Icon\"/>")
          .append("<div>")
            .append("<div class=\"achievement-title\">").append(achievement.getTitle()).append("</div>")
            .append("<div class=\"achievement-description\">").append(achievement.getDescription()).append("</div>")
          .append("</div>")
        .append("</div>");
    }

    return achievementsHtml.toString();
  }


  private String convertMinutesToHoursMinutes(double averageTaskDuration) {
    double hours = averageTaskDuration / 60.0;
    double remainingMinutes = averageTaskDuration % 60;
    return String.format(Locale.US, "%.1f hours, %.1f minutes", hours, remainingMinutes);
  }
}
