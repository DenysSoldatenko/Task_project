package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtils.ROLE_DISCREPANCY_FOUND;
import static com.example.taskmanagerproject.utils.MessageUtils.TEAM_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtils.USERS_DO_NOT_HAVE_ROLES_IN_TEAM;
import static com.example.taskmanagerproject.utils.MessageUtils.USERS_NOT_IN_SAME_PROJECT;
import static com.example.taskmanagerproject.utils.MessageUtils.USERS_NOT_IN_SAME_TEAM;
import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.lang.String.format;

import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ProjectNotFoundException;
import com.example.taskmanagerproject.exceptions.TeamNotFoundException;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.RoleHierarchyRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating task data.
 */
@Component
public final class TaskValidator extends BaseValidator<TaskDto> {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final RoleRepository roleRepository;
  private final ProjectRepository projectRepository;
  private final TeamUserRepository teamUserRepository;
  private final RoleHierarchyRepository roleHierarchyRepository;

  /**
   * Constructs a TaskValidator instance.
   *
   * @param validator The validator instance used for constraint validation.
   * @param userRepository The repository for accessing user data.
   * @param teamRepository The repository for accessing team data.
   * @param roleHierarchyRepository The repository for accessing role hierarchy data.
   * @param projectRepository The repository for accessing project data.
   */
  public TaskValidator(
      Validator validator, UserRepository userRepository, TeamRepository teamRepository,
      RoleHierarchyRepository roleHierarchyRepository, ProjectRepository projectRepository,
      RoleRepository roleRepository, TeamUserRepository teamUserRepository
  ) {
    super(validator);
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.roleHierarchyRepository = roleHierarchyRepository;
    this.projectRepository = projectRepository;
    this.roleRepository = roleRepository;
    this.teamUserRepository = teamUserRepository;
  }

  /**
   * Validates a TaskDto object.
   *
   * @param taskDto The TaskDto object to validate.
   * @throws ValidationException If validation fails.
   */
  public void validateTaskDto(TaskDto taskDto) {
    Set<String> errorMessages = new HashSet<>();
    validateConstraints(taskDto, errorMessages);
    validateUsers(taskDto, errorMessages);
    throwIfErrorsExist(errorMessages);
  }

  private void validateUsers(TaskDto taskDto, Set<String> errorMessages) {
    Project project = getProjectByName(taskDto.project().name());
    Team team = getTeamByName(taskDto.team().name());
    User assignedToUser = getUserByUsername(taskDto.assignedTo().username());
    User assignedByUser = getUserByUsername(taskDto.assignedBy().username());

    validateUserRoleHierarchy(assignedByUser, assignedToUser, team, errorMessages);
    validateUsersInSameTeam(assignedByUser, assignedToUser, team, errorMessages);
    validateUsersInSameProject(assignedByUser, assignedToUser, project, errorMessages);
  }

  private void validateUserRoleHierarchy(User assignedByUser, User assignedToUser, Team team, Set<String> errorMessages) {
    Role assignedByRole = roleRepository.getRoleForUserInTeam(assignedByUser.getId(), team.getId());
    Role assignedToRole = roleRepository.getRoleForUserInTeam(assignedToUser.getId(), team.getId());

    if (assignedByRole == null || assignedToRole == null) {
      errorMessages.add(USERS_DO_NOT_HAVE_ROLES_IN_TEAM);
      return;
    }

    if (!roleHierarchyRepository.isHigherRoleAssigned(assignedByRole.getId(), assignedToRole.getId())) {
      errorMessages.add(format(ROLE_DISCREPANCY_FOUND, assignedByRole.getName(), assignedToRole.getName()));
    }
  }

  private void validateUsersInSameTeam(User assignedByUser, User assignedToUser, Team team, Set<String> errorMessages) {
    if (!(teamUserRepository.existsByUserIdAndTeamId(assignedByUser.getId(), team.getId())
        && teamUserRepository.existsByUserIdAndTeamId(assignedToUser.getId(), team.getId()))) {
      errorMessages.add(format(USERS_NOT_IN_SAME_TEAM, assignedByUser.getUsername(), assignedToUser.getUsername()));
    }
  }

  private void validateUsersInSameProject(User assignedByUser, User assignedToUser, Project project, Set<String> errorMessages) {
    boolean isAssignedByUserInProject = projectRepository.findByUserSlug(assignedByUser.getSlug()) != null;
    boolean isAssignedToUserInProject = projectRepository.findByUserSlug(assignedToUser.getSlug()) != null;

    if (!isAssignedByUserInProject || !isAssignedToUserInProject) {
      errorMessages.add(format(USERS_NOT_IN_SAME_PROJECT, assignedByUser.getUsername(), assignedToUser.getUsername(), project.getName()));
    }
  }


  private Project getProjectByName(String projectName) {
    return projectRepository.findByName(projectName)
      .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectName));
  }

  private Team getTeamByName(String teamName) {
    return teamRepository.findByName(teamName)
      .orElseThrow(() -> new TeamNotFoundException(TEAM_NOT_FOUND_WITH_NAME + teamName));
  }

  private User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }
}
