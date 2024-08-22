package com.example.taskmanagerproject.dtos.report;

import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.team.Team;
import java.time.LocalDateTime;

/**
 * A record to encapsulate the validated data (User, Team, Project, and Dates) for the report.
 */
public record ReportData(User user, Team team, Project project, LocalDateTime startDate, LocalDateTime endDate) {}
