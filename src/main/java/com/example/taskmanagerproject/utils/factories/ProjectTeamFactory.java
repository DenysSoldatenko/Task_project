package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.projects.ProjectTeamId;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for handling User-Project associations and constructing ProjectTeam entities.
 */
@Component
@RequiredArgsConstructor
public final class ProjectTeamFactory {

  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;

  /**
   * Creates a list of ProjectTeam entities from the provided list of ProjectTeamDto objects.
   *
   * @param projectTeams The list of ProjectTeamDto objects to create ProjectTeam entities.
   * @return A list of {@link ProjectTeam} entities representing the user-project assignments.
   */
  public List<ProjectTeam> createProjectTeamAssociations(List<ProjectTeamDto> projectTeams) {
    List<ProjectTeam> projectTeamAssociations = new ArrayList<>();

    for (ProjectTeamDto projectTeamDto : projectTeams) {
      Project project = getProjectFromRequest(projectTeamDto);
      Team team = getTeamFromRequest(projectTeamDto);
      projectTeamAssociations.add(buildProjectTeam(project, team));
    }

    return projectTeamAssociations;
  }

  /**
   * Retrieves the Project entity based on the project from the ProjectTeamDto.
   *
   * @param projectTeamDto The ProjectTeamDto containing the project.
   * @return The {@link Project} entity corresponding to the project.
   * @throws ResourceNotFoundException if the project with the specified project is not found.
   */
  private Project getProjectFromRequest(ProjectTeamDto projectTeamDto) {
    return projectRepository.findByName(projectTeamDto.project().name())
        .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectTeamDto.project().name()));
  }

  /**
   * Retrieves the Team entity based on the team from the ProjectTeamDto.
   *
   * @param projectTeamDto The ProjectTeamDto containing the team.
   * @return The {@link Team} entity corresponding to the team.
   * @throws ResourceNotFoundException if the team with the specified team is not found.
   */
  private Team getTeamFromRequest(ProjectTeamDto projectTeamDto) {
    return teamRepository.findByName(projectTeamDto.team().name())
      .orElseThrow(() -> new ResourceNotFoundException(TEAM_NOT_FOUND_WITH_NAME + projectTeamDto.team().name()));
  }

  private ProjectTeam buildProjectTeam(Project project, Team team) {
    ProjectTeam projectTeam = new ProjectTeam();
    projectTeam.setId(new ProjectTeamId(team.getId(), project.getId()));
    projectTeam.setProject(project);
    projectTeam.setTeam(team);
    return projectTeam;
  }
}
