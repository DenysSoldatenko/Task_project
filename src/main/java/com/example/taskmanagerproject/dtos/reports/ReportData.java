package com.example.taskmanagerproject.dtos.reports;

import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import java.time.LocalDateTime;

/**
 * A record to encapsulate the validated data (User, Team, Project, and Dates) for the report.
 */
public record ReportData(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {}
