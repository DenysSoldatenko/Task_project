package com.example.taskmanagerproject.entities.tasks;

/**
 * Enumeration representing the status of a task in the project.
 */
public enum TaskStatus {
  ASSIGNED,         // Task is assigned to a user but not yet started
  IN_PROGRESS,      // Task is currently being worked on
  PENDING_REVIEW,   // Task is awaiting review by a higher role (e.g., Senior Developer or TeamLead)
  APPROVED,         // Task has been reviewed and approved
  REJECTED,         // Task has been reviewed and requires changes
  ON_HOLD,          // Task is temporarily paused
  CANCELLED         // Task has been cancelled and will not be completed
}
