//package com.example.taskmanagerproject.utils.reports;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import java.util.Locale;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockitoAnnotations;
//
//class ReportMetricUtilTest {
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//    Locale.setDefault(Locale.US);
//  }
//
//  @Test
//  void shouldCalculatePercentageWithValidInputs() {
//    double result = ReportMetricUtil.calculatePercentage(50, 200);
//    assertEquals(25.0, result, 0.001);
//  }
//
//  @Test
//  void shouldCalculatePercentageWithZeroDenominator() {
//    double result = ReportMetricUtil.calculatePercentage(50, 0);
//    assertEquals(0.0, result, 0.001);
//  }
//
//  @Test
//  void shouldCalculatePercentageWithStringInputs() {
//    double result = ReportMetricUtil.calculatePercentage("75", "100");
//    assertEquals(75.0, result, 0.001);
//  }
//
//  @Test
//  void shouldThrowNumberFormatExceptionForInvalidNumerator() {
//    assertThrows(NumberFormatException.class, () -> ReportMetricUtil.calculatePercentage("invalid", 100));
//  }
//
//  @Test
//  void shouldThrowNumberFormatExceptionForInvalidDenominator() {
//    assertThrows(NumberFormatException.class, () -> ReportMetricUtil.calculatePercentage(100, "invalid"));
//  }
//
//  @Test
//  void shouldDetermineUserLevelLegendary() {
//    int level = ReportMetricUtil.determineUserLevel(90, 85, 95, 80);
//    assertEquals(5, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelElite() {
//    int level = ReportMetricUtil.determineUserLevel(75, 70, 80, 65);
//    assertEquals(4, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelPro() {
//    int level = ReportMetricUtil.determineUserLevel(60, 55, 65, 50);
//    assertEquals(3, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelRisingStar() {
//    int level = ReportMetricUtil.determineUserLevel(45, 40, 50, 35);
//    assertEquals(2, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelNovice() {
//    int level = ReportMetricUtil.determineUserLevel(30, 25, 35, 20);
//    assertEquals(1, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelWithBoundaryValues() {
//    int level = ReportMetricUtil.determineUserLevel(85, 85, 85, 85); // Score = 85
//    assertEquals(5, level);
//    level = ReportMetricUtil.determineUserLevel(70, 70, 70, 70); // Score = 70
//    assertEquals(4, level);
//    level = ReportMetricUtil.determineUserLevel(55, 55, 55, 55); // Score = 55
//    assertEquals(3, level);
//    level = ReportMetricUtil.determineUserLevel(40, 40, 40, 40); // Score = 40
//    assertEquals(2, level);
//  }
//
//  @Test
//  void shouldDetermineUserLevelWithMaxScoreCapped() {
//    int level = ReportMetricUtil.determineUserLevel(1000, 1000, 1000, 1000); // Score capped at 100
//    assertEquals(5, level);
//  }
//
//  @Test
//  void shouldGetUserLevelNameLegendary() {
//    String name = ReportMetricUtil.getUserLevelName(5);
//    assertEquals("Legendary", name);
//  }
//
//  @Test
//  void shouldGetUserLevelNameElite() {
//    String name = ReportMetricUtil.getUserLevelName(4);
//    assertEquals("Elite", name);
//  }
//
//  @Test
//  void shouldGetUserLevelNamePro() {
//    String name = ReportMetricUtil.getUserLevelName(3);
//    assertEquals("Pro", name);
//  }
//
//  @Test
//  void shouldGetUserLevelNameRisingStar() {
//    String name = ReportMetricUtil.getUserLevelName(2);
//    assertEquals("Rising Star", name);
//  }
//
//  @Test
//  void shouldGetUserLevelNameNovice() {
//    String name = ReportMetricUtil.getUserLevelName(1);
//    assertEquals("Novice", name);
//  }
//
//  @Test
//  void shouldGetUserLevelNameNoviceForInvalidLevel() {
//    String name = ReportMetricUtil.getUserLevelName(0);
//    assertEquals("Novice", name);
//    name = ReportMetricUtil.getUserLevelName(6);
//    assertEquals("Novice", name);
//  }
//
//  @Test
//  void shouldFormatPercentageWithOneDecimal() {
//    String result = ReportMetricUtil.formatPercentage(75.666);
//    assertEquals("75.7", result);
//  }
//
//  @Test
//  void shouldFormatPercentageWithZero() {
//    String result = ReportMetricUtil.formatPercentage(0.0);
//    assertEquals("0.0", result);
//  }
//
//  @Test
//  void shouldFormatDurationInHoursAndMinutes() {
//    String result = ReportMetricUtil.formatDuration(125.5);
//    assertEquals("2.0 hours, 5.5 minutes", result);
//  }
//
//  @Test
//  void shouldFormatDurationWithZeroMinutes() {
//    String result = ReportMetricUtil.formatDuration(120);
//    assertEquals("2.0 hours, 0.0 minutes", result);
//  }
//
//  @Test
//  void shouldFormatDurationWithStringInput() {
//    String result = ReportMetricUtil.formatDuration("90");
//    assertEquals("1.0 hours, 30.0 minutes", result);
//  }
//
//  @Test
//  void shouldFormatPercentageRoundingDown() {
//    String result = ReportMetricUtil.formatPercentage(33.34);
//    assertEquals("33.3", result);
//  }
//
//  @Test
//  void shouldFormatPercentageRoundingUp() {
//    String result = ReportMetricUtil.formatPercentage(99.95);
//    assertEquals("100.0", result);
//  }
//
//  @Test
//  void shouldFormatPercentageWithNegativeValue() {
//    String result = ReportMetricUtil.formatPercentage(-12.345);
//    assertEquals("-12.3", result);
//  }
//
//  @Test
//  void shouldFormatDurationZeroMinutes() {
//    String result = ReportMetricUtil.formatDuration(0);
//    assertEquals("0.0 hours, 0.0 minutes", result);
//  }
//
//  @Test
//  void shouldFormatDurationWithFractionalMinutes() {
//    String result = ReportMetricUtil.formatDuration(61.75);
//    assertEquals("1.0 hours, 1.8 minutes", result);
//  }
//
//  @Test
//  void shouldFormatDurationWithLargeNumber() {
//    String result = ReportMetricUtil.formatDuration(1500);
//    assertEquals("25.0 hours, 0.0 minutes", result);
//  }
//
//  @Test
//  void shouldThrowNumberFormatExceptionForInvalidDuration() {
//    assertThrows(NumberFormatException.class, () -> ReportMetricUtil.formatDuration("invalid"));
//  }
//
//  @Test
//  void shouldFormatRoleNameWithUnderscores() {
//    String result = ReportMetricUtil.formatRoleName("team_leader");
//    assertEquals("Team Leader", result);
//  }
//
//  @Test
//  void shouldFormatRoleNameSingleWord() {
//    String result = ReportMetricUtil.formatRoleName("ADMIN");
//    assertEquals("Admin", result);
//  }
//
//  @Test
//  void shouldFormatRoleNameEmptyString() {
//    String result = ReportMetricUtil.formatRoleName("");
//    assertEquals("", result);
//  }
//
//  @Test
//  void shouldThrowNullPointerExceptionForNullRoleName() {
//    assertThrows(NullPointerException.class, () -> ReportMetricUtil.formatRoleName(null));
//  }
//
//  @Test
//  void shouldFormatChartDateFullFormat() {
//    String result = ReportMetricUtil.formatChartDate("2025-06-24T10:33:00");
//    assertEquals("06-24", result);
//  }
//
//  @Test
//  void shouldFormatChartDateShortFormat() {
//    String result = ReportMetricUtil.formatChartDate("2025-06-24");
//    assertEquals("06-24", result);
//  }
//
//  @Test
//  void shouldFormatChartDatePartialFormat() {
//    String result = ReportMetricUtil.formatChartDate("2025-06");
//    assertEquals("06", result);
//  }
//
//  @Test
//  void shouldFormatChartDateVeryShortFormat() {
//    String result = ReportMetricUtil.formatChartDate("2025");
//    assertEquals("2025", result);
//  }
//
//  @Test
//  void shouldFormatChartDateWithObjectInput() {
//    String result = ReportMetricUtil.formatChartDate(new Object() {
//      @Override
//      public String toString() {
//        return "2025-06-24";
//      }
//    });
//    assertEquals("06-24", result);
//  }
//
//  @Test
//  void shouldThrowNullPointerExceptionForNullDate() {
//    assertThrows(NullPointerException.class, () -> ReportMetricUtil.formatChartDate(null));
//  }
//}