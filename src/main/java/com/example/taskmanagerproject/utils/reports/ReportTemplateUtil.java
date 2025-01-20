package com.example.taskmanagerproject.utils.reports;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.joining;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.experimental.UtilityClass;

/**
 * Utility class for generating report-related HTML templates.
 * This class provides static methods for generating various HTML templates, such as
 * stars, achievements, user images, and others.
 */
@UtilityClass
public class ReportTemplateUtil {

  /**
   * Generates an HTML representation of stars based on a given rating.
   * The stars are represented by filled or empty icons, depending on the rating value.
   * The rating is expected to be a number between 0 and 5.
   *
   * @param rating The rating value (0 to 5).
   * @return The HTML string representing the stars based on the rating.
   */
  public static String generateStarsHtml(int rating) {
    String filledStar = "<img src=\"https://img.icons8.com/?id=7856\" alt=\"Filled Star\" class=\"star-icon\"/>";
    String emptyStar = "<img src=\"https://img.icons8.com/ios/452/star.png\" alt=\"Empty Star\" class=\"star-icon\"/>";
    return filledStar.repeat(rating) + emptyStar.repeat(5 - rating);
  }

  /**
   * Generates an HTML representation of achievements.
   * The HTML string contains a list of achievement items with their image, title, and description.
   * If no achievements are provided, a default message will be returned.
   *
   * @param achievements A list of Achievement objects to be converted to HTML.
   * @return The HTML string representing the achievements.
   */
  public static String generateAchievementsHtml(List<Achievement> achievements) {
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

  /**
   * Generates an HTML representation of a user's image.
   * If the image URL is a placeholder (in other words, it contains "dummyimage.com"), the URL is returned as-is.
   * Otherwise, the method constructs a full URL to fetch the image from the local server.
   *
   * @param userImageUrl The image URL of the user.
   * @return The HTML string representing the user's image.
   */
  public static String generateImageUser(String userImageUrl) {
    return "<img src=\"" + (userImageUrl.contains("dummyimage.com") ? userImageUrl : "http://127.0.0.1:9000/images/" + userImageUrl) + "\" alt=\"User Image\" class=\"user-img\"><br/>\n";
  }

  /**
   * Generates an HTML representation of a task completion chart.
   * The chart displays task completion rates for each task in a list of metrics.
   * The chart is generated with bars representing the completion percentage of each task.
   * Additionally, the highest and lowest completion rates are highlighted.
   *
   * @param metrics A list of Object arrays representing task metrics. The first element of the array is the date,
   *                and the second element is the task completion percentage.
   * @return The HTML string representing the task completion chart.
   */
  public static String generateChartHtml(List<Object[]> metrics) {
    StringJoiner chartHtml = new StringJoiner("", "", "");
    StringBuilder currentChartHtml = new StringBuilder();
    int barCount = 0;
    int highestIndex = -1;
    int lowestIndex = -1;
    double highestPercentage = -1;
    double lowestPercentage = Double.MAX_VALUE;

    for (int i = 0; i < metrics.size(); i++) {
      double completion = parseDouble(metrics.get(i)[1].toString());
      if (completion > highestPercentage) {
        highestPercentage = completion;
        highestIndex = i;
      }
      if (completion != 0.0 && completion < lowestPercentage) {
        lowestPercentage = completion;
        lowestIndex = i;
      }
    }

    for (int i = 0; i < metrics.size(); i++) {
      Object[] data = metrics.get(i);
      String date = ReportMetricUtil.formatChartDate(data[0].toString());
      double completion = parseDouble(data[1].toString());
      String percentage = ReportMetricUtil.formatPercentage(completion);

      if (barCount % 12 == 0 && !currentChartHtml.isEmpty()) {
        chartHtml.add("<div class=\"chart-container\">" + currentChartHtml + "</div>");
        currentChartHtml.setLength(0);
        barCount = 0;
      }
      int left = barCount * 55;
      String createNonCompletionBar = createNonCompletionBar(left);
      String createCompletionBar = createCompletionBars(left, percentage, getTaskClass(i, highestIndex, lowestIndex));
      currentChartHtml.append(completion == 0.0 ? createNonCompletionBar : createCompletionBar).append(createChartLabel(left, date));
      barCount++;
    }

    if (!currentChartHtml.isEmpty()) {
      chartHtml.add("<div class=\"chart-container\">" + currentChartHtml + "</div>");
    }
    return chartHtml.toString();
  }

  private static String getTaskClass(int index, int highestIndex, int lowestIndex) {
    if (index == highestIndex) {
      return "task-completed task-highest";
    } else if (index == lowestIndex) {
      return "task-completed task-lowest";
    }
    return "task-completed";
  }

  private static String createNonCompletionBar(int left) {
    return "<div class=\"chart-bar task-non\" style=\"height: 100px; left: " + left + "px;\">" + "<div class=\"percentage\">0%</div></div>";
  }

  private static String createChartLabel(int left, String date) {
    return "<div class=\"chart-label\" style=\"left: " + left + "px;\">" + date + "</div>";
  }

  private static String createCompletionBars(int left, String percentage, String taskClass) {
    return "<div class=\"chart-bar task-created\" style=\"height: 100px; left: " + left + "px;\"></div>" + "<div class=\"chart-bar " + taskClass
           + "\" style=\"height: " + percentage + "px; left: " + left + "px;\">" + "<div class=\"percentage\">" + percentage + "%</div></div>";
  }

  /**
   * Generates HTML for a team member's report, including image, name, role, task metrics,
   * progress bars for task completion, bug fixes, critical tasks, and achievements count.
   *
   * @param data An array of team member metrics.
   * @return A String representing the HTML content for the team member's report.
   */
  public String generateTeamMemberHtml(Object[] data) {
    return String.format("""
        <div class="team-member">
            %s
            <div>
                <h2>%s</h2>
                <p>%s</p>

                <table class="stats-table">
                    <tr><th>Tasks Completed</th><th>On-Time Completion</th><th>Avg Task Duration</th></tr>
                    <tr><td>%s</td><td>%s</td><td>%s</td></tr>
                </table>

                <div class="metrics">
                    %s %s %s
                </div>

                <div class="achievements">
                    <p><strong>Achievements:</strong> %s</p>
                </div>
            </div>
        </div>
        """,
      ReportTemplateUtil.generateImageUser((String) data[1]),
      data[0], // Member Name
      ReportMetricUtil.formatRoleName(data[2]),
      data[4] + "/" + data[3], // Tasks Completed
      data[5] + "/" + data[4], // On-Time Tasks
      ReportMetricUtil.formatDuration(Double.parseDouble(data[6].toString())), // Avg Task Duration
      generateProgressBar("Task Completion", "task-progress", data[7]),
      generateProgressBar("Bugfix Progress", "bug-progress", data[8]),
      generateProgressBar("Critical Task Completion", "critical-progress", data[9]),
      data[10] // Number of Achievements
    );
  }

  private String generateProgressBar(String title, String cssClass, Object percentage) {
    String progress = ReportMetricUtil.formatPercentage(((BigDecimal) percentage).doubleValue());
    return String.format("""
        <div class="metric">
            <h3>%s</h3>
            <div class="progress-bar">
                <div class="progress %s" style="width: %s%%;">%s%%</div>
            </div>
        </div>
        """, title, cssClass, progress, progress);
  }

  /**
   * Generates an HTML table row for each project team member displaying their metrics.
   * The metrics include completed tasks, on-time deliveries, critical issues,
   * total defects, and key achievements.
   *
   * @param memberData a list of object arrays where each array contains metrics for a team member.
   * @return a string of HTML table rows for all team members.
   */
  public String generateProjectMemberHtml(List<Object[]> memberData) {
    StringBuilder html = new StringBuilder();

    for (Object[] data : memberData) {
      html.append(String.format("""
          <tr>
              <td><strong>%s</strong></td>
              <td>
                  <div class="metric"><img src="https://img.icons8.com/?id=40318&format=png" alt="Done Tasks"/><strong>Completed Tasks:</strong> %s</div>
                  <div class="metric"><img src="https://img.icons8.com/?id=63256&format=png" alt="Schedule"/><strong>On-Time Deliveries:</strong> %s</div>
                  <div class="metric"><img src="https://img.icons8.com/?id=ocWODnkQ9bjZ&format=png" alt="Critical"/><strong>Critical Issues:</strong> %s</div>
              </td>
              <td>
                  <div class="metric"><img src="https://img.icons8.com/?id=PhEAsgstvfAw&format=png" alt="Defects"/><strong>Total Defects:</strong> %s</div>
                  <div class="metric"><img src="https://img.icons8.com/?id=VUt5dWfcfFzt&format=png" alt="Achievements"/><strong>Key Achievements:</strong> %s</div>
              </td>
          </tr>
          """,
          data[0], data[3] + "/" + data[2], data[4] + "/" + data[3], data[6] + "/" + data[5], data[8] + "/" + data[7], data[1]
        )
      );
    }
    return html.toString();
  }

  /**
   * Replaces placeholders in the given template string with values from a map.
   * The placeholders are expected to be in the form of keys in the template, which will be replaced with corresponding
   * values from the provided map.
   *
   * @param template The template string containing placeholders.
   * @param placeholders A map of placeholder keys and their corresponding values.
   * @return The template string with the placeholders replaced by their values.
   */
  public static String replacePlaceholders(String template, Map<String, String> placeholders) {
    for (var entry : placeholders.entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue());
    }
    return template;
  }
}
