package com.example.taskmanagerproject.utils.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import com.example.taskmanagerproject.entities.achievements.Achievement;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class ReportTemplateUtilTest {

  private MockedStatic<ReportMetricUtil> metricUtilMock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    metricUtilMock = mockStatic(ReportMetricUtil.class);
  }

  @AfterEach
  void tearDown() {
    metricUtilMock.close();
  }

  @Test
  void shouldGenerateStarsHtmlWithValidRating() {
    String result = ReportTemplateUtil.generateStarsHtml(3);
    String expected = "<img src=\"https://img.icons8.com/?id=7856\" alt=\"Filled Star\" class=\"star-icon\"/>".repeat(3)
        + "<img src=\"https://img.icons8.com/ios/452/star.png\" alt=\"Empty Star\" class=\"star-icon\"/>".repeat(2);
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateStarsHtmlWithZeroRating() {
    String result = ReportTemplateUtil.generateStarsHtml(0);
    String expected = "<img src=\"https://img.icons8.com/ios/452/star.png\" alt=\"Empty Star\" class=\"star-icon\"/>".repeat(5);
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateStarsHtmlWithMaxRating() {
    String result = ReportTemplateUtil.generateStarsHtml(5);
    String expected = "<img src=\"https://img.icons8.com/?id=7856\" alt=\"Filled Star\" class=\"star-icon\"/>".repeat(5);
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateAchievementsHtmlWithAchievements() {
    List<Achievement> achievements = List.of(
        new Achievement(2L, "Second Milestone", "Completed 100 tasks in total.", "task_master.png"),
        new Achievement(12L, "Bug Slayer", "Resolved 20+ critical bugs in a sprint.", "bug_slayer.png")
    );
    String result = ReportTemplateUtil.generateAchievementsHtml(achievements);
    String expected = "<div class=\"achievement\">"
        + "<img src=\"task_master.png\" alt=\"Achievement Icon\"/>"
        + "<div>"
        + "<div class=\"achievement-title\">Second Milestone</div>"
        + "<div class=\"achievement-description\">Completed 100 tasks in total.</div>"
        + "</div>"
        + "</div>"
        + "<div class=\"achievement\">"
        + "<img src=\"bug_slayer.png\" alt=\"Achievement Icon\"/>"
        + "<div>"
        + "<div class=\"achievement-title\">Bug Slayer</div>"
        + "<div class=\"achievement-description\">Resolved 20+ critical bugs in a sprint.</div>"
        + "</div>"
        + "</div>";

    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateAchievementsHtmlWithEmptyList() {
    String result = ReportTemplateUtil.generateAchievementsHtml(Collections.emptyList());
    String expected = "<div class=\"no-achievements\">There are no achievements yet.</div>";
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateImageUserWithDummyUrl() {
    String userImageUrl = "https://dummyimage.com/100x100.png";
    String result = ReportTemplateUtil.generateImageUser(userImageUrl);
    String expected = "<img src=\"https://dummyimage.com/100x100.png\" alt=\"User Image\" class=\"user-img\"><br/>\n";
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateImageUserWithLocalUrl() {
    String userImageUrl = "user.jpg";
    String result = ReportTemplateUtil.generateImageUser(userImageUrl);
    String expected = "<img src=\"http://127.0.0.1:9000/images/user.jpg\" alt=\"User Image\" class=\"user-img\"><br/>\n";
    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateChartHtmlWithMetrics() {
    List<Object[]> metrics = List.of(
        new Object[]{"2025-06-01", 80.0},
        new Object[]{"2025-06-02", 50.0},
        new Object[]{"2025-06-03", 0.0}
    );
    metricUtilMock.when(() -> ReportMetricUtil.formatChartDate("2025-06-01")).thenReturn("06-01");
    metricUtilMock.when(() -> ReportMetricUtil.formatChartDate("2025-06-02")).thenReturn("06-02");
    metricUtilMock.when(() -> ReportMetricUtil.formatChartDate("2025-06-03")).thenReturn("06-03");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(80.0)).thenReturn("80.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(50.0)).thenReturn("50.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(0.0)).thenReturn("0.0");

    String result = ReportTemplateUtil.generateChartHtml(metrics);
    String expected = "<div class=\"chart-container\">"
        + "<div class=\"chart-bar task-created\" style=\"height: 100px; left: 0px;\"></div>"
        + "<div class=\"chart-bar task-completed task-highest\" style=\"height: 80.0px; left: 0px;\"><div class=\"percentage\">80.0%</div></div>"
        + "<div class=\"chart-label\" style=\"left: 0px;\">06-01</div>"
        + "<div class=\"chart-bar task-created\" style=\"height: 100px; left: 55px;\"></div>"
        + "<div class=\"chart-bar task-completed task-lowest\" style=\"height: 50.0px; left: 55px;\"><div class=\"percentage\">50.0%</div></div>"
        + "<div class=\"chart-label\" style=\"left: 55px;\">06-02</div>"
        + "<div class=\"chart-bar task-non\" style=\"height: 100px; left: 110px;\"><div class=\"percentage\">0%</div></div>"
        + "<div class=\"chart-label\" style=\"left: 110px;\">06-03</div>"
        + "</div>";

    assertEquals(expected, result);
  }

  @Test
  void shouldGenerateChartHtmlWithEmptyMetrics() {
    String result = ReportTemplateUtil.generateChartHtml(Collections.emptyList());
    assertEquals("", result);
  }

  @Test
  void shouldGenerateChartHtmlWithMultipleContainers() {
    List<Object[]> metrics = Collections.nCopies(13, new Object[]{"2025-06-01", 50.0});
    metricUtilMock.when(() -> ReportMetricUtil.formatChartDate("2025-06-01")).thenReturn("06-01");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(50.0)).thenReturn("50.0");

    String result = ReportTemplateUtil.generateChartHtml(metrics);
    assertTrue(result.contains("<div class=\"chart-container\">"));
    assertEquals(3, result.split("<div class=\"chart-container\">").length); // 12 bars + 1 bar = 2 containers
  }

  @Test
  void shouldGenerateTeamMemberHtml() {
    Object[] data = new Object[]{
        "User1", "user1.jpg", "TEAM_LEADER", 100L, 80L, 60L, 7200.0,
        new BigDecimal("80.0"), new BigDecimal("70.0"), new BigDecimal("90.0"), 5L
    };
    metricUtilMock.when(() -> ReportMetricUtil.formatRoleName("TEAM_LEADER")).thenReturn("Team Leader");
    metricUtilMock.when(() -> ReportMetricUtil.formatDuration(7200.0)).thenReturn("120.0 hours, 0.0 minutes");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(80.0)).thenReturn("80.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(70.0)).thenReturn("70.0");
    metricUtilMock.when(() -> ReportMetricUtil.formatPercentage(90.0)).thenReturn("90.0");

    String result = ReportTemplateUtil.generateTeamMemberHtml(data);
    String expected = """
<div class="team-member">
    <img src="http://127.0.0.1:9000/images/user1.jpg" alt="User Image" class="user-img"><br/>

    <div>
        <h2>User1</h2>
        <p>Team Leader</p>

        <table class="stats-table">
            <tr><th>Tasks Completed</th><th>On-Time Completion</th><th>Avg Task Duration</th></tr>
            <tr><td>80/100</td><td>60/80</td><td>120.0 hours, 0.0 minutes</td></tr>
        </table>

        <div class="metrics">
            <div class="metric">
    <h3>Task Completion</h3>
    <div class="progress-bar">
        <div class="progress task-progress" style="width: 80.0%;">80.0%</div>
    </div>
</div>
 <div class="metric">
    <h3>Bugfix Progress</h3>
    <div class="progress-bar">
        <div class="progress bug-progress" style="width: 70.0%;">70.0%</div>
    </div>
</div>
 <div class="metric">
    <h3>Critical Task Completion</h3>
    <div class="progress-bar">
        <div class="progress critical-progress" style="width: 90.0%;">90.0%</div>
    </div>
</div>

        </div>

        <div class="achievements">
            <p><strong>Achievements:</strong> 5</p>
        </div>
    </div>
</div>
        """;
    assertEquals(expected.trim(), result.trim());
  }

  @Test
  void shouldThrowNullPointerExceptionForNullTeamMemberData() {
    assertThrows(NullPointerException.class, () -> ReportTemplateUtil.generateTeamMemberHtml(null));
  }

  @Test
  void shouldGenerateProjectMemberHtml() {
    List<Object[]> memberData = List.<Object[]>of(
      new Object[]{"User1", 5L, 100L, 80L, 60L, 30L, 20L, 50L, 40L}
    );
    String result = ReportTemplateUtil.generateProjectMemberHtml(memberData);
    String expected = """
        <tr>
            <td><strong>User1</strong></td>
            <td>
                <div class="metric"><img src="https://img.icons8.com/?id=40318&format=png" alt="Done Tasks"/><strong>Completed Tasks:</strong> 80/100</div>
                <div class="metric"><img src="https://img.icons8.com/?id=63256&format=png" alt="Schedule"/><strong>On-Time Deliveries:</strong> 60/80</div>
                <div class="metric"><img src="https://img.icons8.com/?id=ocWODnkQ9bjZ&format=png" alt="Critical"/><strong>Critical Issues:</strong> 20/30</div>
            </td>
            <td>
                <div class="metric"><img src="https://img.icons8.com/?id=PhEAsgstvfAw&format=png" alt="Defects"/><strong>Total Defects:</strong> 40/50</div>
                <div class="metric"><img src="https://img.icons8.com/?id=VUt5dWfcfFzt&format=png" alt="Achievements"/><strong>Key Achievements:</strong> 5</div>
            </td>
        </tr>
        """;
    assertEquals(expected.trim(), result.trim());
  }

  @Test
  void shouldGenerateProjectMemberHtmlWithEmptyList() {
    String result = ReportTemplateUtil.generateProjectMemberHtml(Collections.emptyList());
    assertEquals("", result);
  }

  @Test
  void shouldReplacePlaceholders() {
    String template = "Hello {name}, you are {role}!";
    Map<String, String> placeholders = Map.of("{name}", "User1", "{role}", "Team Leader");
    String result = ReportTemplateUtil.replacePlaceholders(template, placeholders);
    assertEquals("Hello User1, you are Team Leader!", result);
  }

  @Test
  void shouldHandleEmptyPlaceholders() {
    String template = "Hello {name}!";
    Map<String, String> placeholders = Collections.emptyMap();
    String result = ReportTemplateUtil.replacePlaceholders(template, placeholders);
    assertEquals("Hello {name}!", result);
  }

  @Test
  void shouldHandleNullTemplateInReplacePlaceholders() {
    Map<String, String> placeholders = Map.of("{name}", "User1");
    assertThrows(NullPointerException.class, () -> ReportTemplateUtil.replacePlaceholders(null, placeholders));
  }
}