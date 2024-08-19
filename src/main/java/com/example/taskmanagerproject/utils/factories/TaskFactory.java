package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;

import com.example.taskmanagerproject.dtos.task.TaskDto;
import com.example.taskmanagerproject.entities.project.Project;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.entities.task.Task;
import com.example.taskmanagerproject.entities.team.Team;
import com.example.taskmanagerproject.exceptions.ProjectNotFoundException;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating Task instances.
 */
@Component
@RequiredArgsConstructor
public final class TaskFactory {

  private final ProjectRepository projectRepository;
  private final TeamRepository teamRepository;
  private final UserRepository userRepository;

  /**
   * Creates a new Task entity from a TaskDto.
   *
   * @param taskDto The TaskDto containing the task details.
   * @return A new Task entity.
   */
  public Task createTaskFromDto(TaskDto taskDto) {
    Project project = getProjectByName(taskDto.project().name());
    Team team = getTeamByName(taskDto.team().name());
    User assignedToUser = getUserByUsername(taskDto.assignedTo().username());
    User assignedByUser = getUserByUsername(taskDto.assignedBy().username());

    return buildTask(taskDto, project, team, assignedToUser, assignedByUser);
  }

  /**
   * Retrieves a Project entity based on the project name.
   *
   * @param projectName The name of the project to retrieve.
   * @return The Project entity.
   */
  private Project getProjectByName(String projectName) {
    return projectRepository.findByName(projectName)
      .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectName));
  }

  /**
   * Retrieves a Team entity based on the team name.
   *
   * @param teamName The name of the team to retrieve.
   * @return The Team entity.
   */
  private Team getTeamByName(String teamName) {
    return teamRepository.findByName(teamName)
      .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));
  }

  /**
   * Retrieves a User entity based on the username.
   *
   * @param username The username of the user to retrieve.
   * @return The User entity.
   */
  private User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  /**
   * Builds a Task entity from the provided details.
   *
   * @param taskDto       The TaskDto containing task details.
   * @param project       The associated project.
   * @param team          The associated team.
   * @param assignedTo    The user assigned to the task.
   * @param assignedBy    The user who assigned the task.
   * @return A new Task entity.
   */
  private Task buildTask(TaskDto taskDto, Project project, Team team, User assignedTo, User assignedBy) {
    return Task.builder()
      .title(taskDto.title())
      .description(taskDto.description())
      .expirationDate(taskDto.expirationDate())
      .taskStatus(taskDto.taskStatus())
      .priority(taskDto.priority())
      .assignedTo(assignedTo)
      .assignedBy(assignedBy)
      .project(project)
      .team(team)
      .build();
  }
}
