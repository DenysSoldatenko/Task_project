package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectTeamFactoryTest {

  @Mock
  private TeamRepository teamRepository;
  @Mock
  private ProjectRepository projectRepository;

  @InjectMocks
  private ProjectTeamFactory projectTeamFactory;

  private Team team;
  private Project project;
  private ProjectTeamDto projectTeamDto;

  @BeforeEach
  void setUp() {
    UserDto userDto = new UserDto(1L, "Test User", "user@gmail.com", "user-slug", "", List.of(""));
    ProjectDto projectDto = new ProjectDto(1L, "Test Project", "Description", userDto);
    TeamDto teamDto = new TeamDto(1L, "Test Team", "Description", userDto);
    projectTeamDto = new ProjectTeamDto(teamDto, projectDto);

    project = new Project();
    project.setId(1L);
    project.setName("Test Project");

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");
  }

  @Test
  void createProjectTeamAssociations_shouldCreateAssociations() {
    when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));

    List<ProjectTeam> result = projectTeamFactory.createProjectTeamAssociations(List.of(projectTeamDto));

    assertEquals(1, result.size());
    ProjectTeam projectTeam = result.get(0);
    assertEquals(project, projectTeam.getProject());
    assertEquals(team, projectTeam.getTeam());
    assertEquals(project.getId(), projectTeam.getProject().getId());
    assertEquals(team.getId(), projectTeam.getTeam().getId());
  }

  @Test
  void createProjectTeamAssociations_shouldThrowIfProjectNotFound() {
    when(projectRepository.findByName("Test Project")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> projectTeamFactory.createProjectTeamAssociations(List.of(projectTeamDto)));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + "Test Project", ex.getMessage());
  }

  @Test
  void createProjectTeamAssociations_shouldThrowIfTeamNotFound() {
    when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> projectTeamFactory.createProjectTeamAssociations(List.of(projectTeamDto)));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + "Test Team", ex.getMessage());
  }

  @Test
  void createProjectTeamAssociations_shouldHandleMultipleDtos() {
    Project project2 = new Project();
    project2.setId(2L);
    project2.setName("Another Project");

    Team team2 = new Team();
    team2.setId(2L);
    team2.setName("Another Team");

    when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));
    when(projectRepository.findByName("Another Project")).thenReturn(Optional.of(project2));
    when(teamRepository.findByName("Another Team")).thenReturn(Optional.of(team2));

    UserDto userDto = new UserDto(1L, "Test User", "user@gmail.com", "user-slug", "", List.of(""));
    ProjectDto projectDto2 = new ProjectDto(2L, "Another Project", "Description", userDto);
    TeamDto teamDto2 = new TeamDto(2L, "Another Team", "Description", userDto);
    ProjectTeamDto projectTeamDto2 = new ProjectTeamDto(teamDto2, projectDto2);
    List<ProjectTeam> result = projectTeamFactory.createProjectTeamAssociations(List.of(projectTeamDto, projectTeamDto2));

    assertEquals(2, result.size());
    assertEquals(project, result.get(0).getProject());
    assertEquals(team, result.get(0).getTeam());
    assertEquals(project2, result.get(1).getProject());
    assertEquals(team2, result.get(1).getTeam());
  }
}