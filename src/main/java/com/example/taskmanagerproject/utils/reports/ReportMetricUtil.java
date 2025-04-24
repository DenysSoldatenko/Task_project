package com.example.taskmanagerproject.utils.reports;

import static java.lang.Character.toUpperCase;
import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Locale;
import lombok.experimental.UtilityClass;

/**
 * Utility service for calculating various report metrics.
 */
@UtilityClass
public class ReportMetricUtil {

  /**
   * Calculates the percentage of a numerator over a denominator.
   *
   * @param numerator   The numerator value.
   * @param denominator The denominator value.
   * @return The calculated percentage, or 0 if the denominator is zero.
   */
  public double calculatePercentage(Object numerator, Object denominator) {
    double num = parseDouble(numerator.toString());
    double denom = parseDouble(denominator.toString());
    return denom == 0 ? 0 : (num / denom) * 100;
  }

  /**
   * Determines the user level based on weighted performance metrics.
   *
   * @param completionRate     Task completion rate (0-100).
   * @param bugFixRate         Bugfix success rate (0-100).
   * @param approvalRate       Approval success rate (0-100).
   * @param criticalResolution Critical resolution rate (0-100).
   * @return User level (1-5).
   */
  public int determineUserLevel(double completionRate, double bugFixRate, double approvalRate, double criticalResolution) {
    double score = Math.min(100, (completionRate * 0.4) + (bugFixRate * 0.3) + (approvalRate * 0.2) + (criticalResolution * 0.1));
    return score >= 85 ? 5 : score >= 70 ? 4 : score >= 55 ? 3 : score >= 40 ? 2 : 1;
  }

  /**
   * Retrieves the user level name based on the level number.
   *
   * @param level The user level (1-5).
   * @return The corresponding level name.
   */
  public String getUserLevelName(int level) {
    return switch (level) {
      case 5 -> "Legendary";
      case 4 -> "Elite";
      case 3 -> "Pro";
      case 2 -> "Rising Star";
      default -> "Novice";
    };
  }

  /**
   * Formats a percentage value with one decimal place.
   *
   * @param value The percentage value.
   * @return A formatted string with "%" appended.
   */
  public String formatPercentage(double value) {
    return format(Locale.US, "%.1f", value);
  }

  /**
   * Formats a duration (in minutes) into a string representing hours and minutes.
   * The duration is first converted from minutes to hours and minutes.
   *
   * @param duration The duration to be formatted, expected to be in minutes.
   * @return A string representing the formatted duration in hours and minutes.
   */
  public static String formatDuration(Object duration) {
    double totalMinutes = parseDouble(duration.toString());
    int wholeHours = (int) (totalMinutes / 60);
    double remainingMinutes = totalMinutes % 60;

    return String.format(Locale.US, "%.1f hours, %.1f minutes", (double) wholeHours, remainingMinutes);
  }

  /**
   * Formats a role name by replacing underscores with spaces and capitalizing the first letter.
   *
   * @param roleName The role name to be formatted.
   * @return The formatted role name.
   */
  public String formatRoleName(Object roleName) {
    String role = roleName.toString().toLowerCase();
    return Arrays.stream(role.split("_"))
      .filter(word -> !word.isEmpty()) // skip empty words
      .map(word -> toUpperCase(word.charAt(0)) + word.substring(1))
      .collect(joining(" "));
  }

  /**
   * Formats the date by extracting the month and day from the provided date string.
   * The format of the date is expected to be at least "yyyy-MM-dd" (or a similar pattern).
   * The method extracts and returns a substring representing the month and day (i.e., "MM-dd").
   *
   * @param date The date object to format. It is expected to be convertible to a string.
   * @return A formatted string representing the month and day in "MM-dd" format, or the
   *         substring starting from the 5th character if the date string is shorter.
   */
  public String formatChartDate(Object date) {
    String dateString = date.toString();
    return (dateString.length() >= 10) ? dateString.substring(5, 10)
      : (dateString.length() >= 5) ? dateString.substring(5)
      : dateString;
  }
}
