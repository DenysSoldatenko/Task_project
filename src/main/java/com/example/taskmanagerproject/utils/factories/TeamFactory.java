package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;
import static java.time.LocalDateTime.now;

import com.example.taskmanagerproject.dtos.TeamDto;
import com.example.taskmanagerproject.entities.Project;
import com.example.taskmanagerproject.entities.Team;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating Team instances from team creation requests.
 */
@Component
@RequiredArgsConstructor
public final class TeamFactory {

  private final UserRepository userRepository;

  /**
   * Creates a new project from the provided ProjectDto.
   *
   * @param request The data transfer object containing the project details,
   *                such as project name, description, and creator information.
   * @return A {@link Project} entity representing the created project with its associated details.
   * @throws UsernameNotFoundException if the creator's username (slug) is not found.
   */
  public Team createProjectFromRequest(final TeamDto request) {
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
  private User getUserFromRequest(final TeamDto request) {
    String username = request.creator().username();
    return userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_USERNAME + username));
  }

  private Team buildProjectFromRequest(final TeamDto request, final User creator) {
    Team team = new Team();
    team.setName(request.name());
    team.setDescription(request.description());
    team.setCreatedAt(now());
    team.setCreator(creator);
    return team;
  }
}
