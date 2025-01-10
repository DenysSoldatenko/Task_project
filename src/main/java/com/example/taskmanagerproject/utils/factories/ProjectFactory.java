package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating Project instances from project creation requests.
 */
@Component
@RequiredArgsConstructor
public final class ProjectFactory {

  private final UserRepository userRepository;

  /**
   * Creates a new project from the provided ProjectDto.
   *
   * @param request The data transfer object containing the project details,
   *                such as project name, description, and creator information.
   * @return A {@link Project} entity representing the created project with its associated details.
   * @throws UsernameNotFoundException if the creator's username (slug) is not found.
   */
  public Project createProjectFromRequest(ProjectDto request) {
    User creator = getUserFromRequest(request);
    return buildProjectFromRequest(request, creator);
  }

  /**
   * Retrieves the User entity based on the creator's username from the request.
   *
   * @param request The ProjectDto containing the creator's username.
   * @return The {@link User} entity corresponding to the creator.
   * @throws UsernameNotFoundException if the user with the specified username is not found.
   */
  private User getUserFromRequest(ProjectDto request) {
    String username = request.creator().username();
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  /**
   * Builds a Project entity from the provided ProjectDto and creator.
   *
   * @param request The ProjectDto containing the project details.
   * @param creator The {@link User} who created the project.
   * @return A new {@link Project} entity populated with the provided details.
   */
  private Project buildProjectFromRequest(ProjectDto request, User creator) {
    Project project = new Project();
    project.setName(request.name());
    project.setDescription(request.description());
    project.setCreator(creator);
    project.setCreatedAt(now());
    return project;
  }
}
