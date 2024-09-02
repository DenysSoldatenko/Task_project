package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.generatePdfFromHtml;
import static com.example.taskmanagerproject.utils.factories.PdfGenerationFactory.loadTemplate;
import static java.lang.Character.toUpperCase;
import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.PdfGenerationException;
import com.example.taskmanagerproject.repositories.AchievementRepository;
import com.example.taskmanagerproject.repositories.TaskRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Generates PDF reports from HTML templates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class PdfReportFactory {

  private static final String USER_TEMPLATE_PATH = "src/main/resources/report_templates/user_template.html";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final TaskRepository taskRepository;
  private final TeamUserRepository teamUserRepository;
  private final AchievementRepository achievementRepository;

  /**
   * Generates a PDF report for a user based on task metrics within a specific project and team during a given date range.
   *
   * @param user       The user for whom the report is generated.
   * @param team       The team the user is a part of.
   * @param project    The project within which the user is working.
   * @param startDate  The start date of the period for which the report is generated.
   * @param endDate    The end date of the period for which the report is generated.
   * @return           A byte array containing the PDF representation of the report.
   * @throws PdfGenerationException If no task metrics are found for the specified user in the given project.
   */
  public byte[] generateReport(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {
    String htmlTemplate = loadTemplate(USER_TEMPLATE_PATH);
    List<Object[]> taskMetricsList = taskRepository.getTaskMetricsByAssignedUser(user.getId(), startDate, endDate, project.getName(), team.getName());

    if (taskMetricsList.isEmpty()) {
      throw new PdfGenerationException("No task metrics found for user " + user.getUsername() + " in project " + project.getName());
    }

    Object[] taskMetrics = taskMetricsList.get(0);
    String populatedHtml = populateTemplate(htmlTemplate, user, team, project, startDate, endDate, taskMetrics);

    return generatePdfFromHtml(populatedHtml);
  }

  private String populateTemplate(String template, User user, Team team, Project project, LocalDateTime start, LocalDateTime end, Object[] metrics) {
    List<Achievement> achievements = achievementRepository.findAchievementsByUserTeamAndProject(user.getId(), team.getId(), project.getId());
    String role = capitalizeRole(teamUserRepository.findRoleByTeamNameAndUsername(team.getName(), user.getUsername()).getName());

    return template.replace("{startDate}", start.format(DATE_FORMATTER))
      .replace("{endDate}", end.format(DATE_FORMATTER))
      .replace("{fullName}", user.getFullName())
      .replace("{email}", user.getUsername())
      .replace("{role}", role)
      .replace("{teamName}", team.getName())
      .replace("{teamObjective}", team.getDescription())
      .replace("{projectName}", project.getName())
      .replace("{projectDescription}", project.getDescription())
      .replace("{tasksCompleted}", metrics[3] + "/" + metrics[2])
      .replace("{taskCompletionRate}", metrics[4].toString())
      .replace("{onTimeTasks}", metrics[5] + "/" + metrics[3])
      .replace("{averageTaskDuration}", formatDuration(metrics[10]))
      .replace("{bugFixesResolved}", metrics[7] + "/" + metrics[6])
      .replace("{criticalTasksSolved}", metrics[9] + "/" + metrics[8])
      .replace("{achievements}", generateAchievementsHtml(achievements));
  }

  private String capitalizeRole(String role) {
    return toUpperCase(role.charAt(0)) + role.substring(1).toLowerCase();
  }

  private String generateAchievementsHtml(List<Achievement> achievements) {
    if (achievements.isEmpty()) {
      return "<div class=\"no-achievements\">There are no achievements yet.</div>";
    }

    return achievements.stream()
      .map(a -> "<div class=\"achievement\">"
        + "<img src=\"" + a.getImageUrl() + "\" alt=\"Achievement Icon\"/>"
        + "<div>"
        + "<div class=\"achievement-title\">" + a.getTitle() + "</div>"
        + "<div class=\"achievement-description\">" + a.getDescription() + "</div>"
        + "</div>"
        + "</div>")
      .collect(joining());
  }

  private String formatDuration(Object duration) {
    double minutes = parseDouble(duration.toString());
    return format(Locale.US, "%.1f hours, %.1f minutes", minutes / 60, minutes % 60);
  }
}
