package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.teams.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProjectTeamMapperTest {

  @Mock
  private ProjectMapper projectMapper;

  @Mock
  private TeamMapper teamMapper;

  @InjectMocks
  private ProjectTeamMapperImpl projectTeamMapper;

  private Project project;
  private ProjectTeam projectTeam;
  private ProjectTeamDto projectTeamDto;

  private Team team;
  private TeamDto teamDto;
  private ProjectDto projectDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    project = new Project();
    project.setId(1L);
    project.setName("Test Project");

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");

    projectTeam = new ProjectTeam();
    projectTeam.setProject(project);
    projectTeam.setTeam(team);

    projectDto = new ProjectDto(1L, "Test Project", "Description", null);
    teamDto = new TeamDto(1L, "Test Team", "Description", null);
    projectTeamDto = new ProjectTeamDto(teamDto, projectDto);
  }

  @Test
  void shouldMapProjectTeamToDto() {
    when(projectMapper.toDto(project)).thenReturn(projectDto);
    when(teamMapper.toDto(team)).thenReturn(teamDto);

    ProjectTeamDto result = projectTeamMapper.toDto(projectTeam);

    assertEquals(projectDto, result.project());
    assertEquals(teamDto, result.team());
  }

  @Test
  void shouldMapProjectTeamDtoToEntity() {
    when(projectMapper.toEntity(projectDto)).thenReturn(project);
    when(teamMapper.toEntity(teamDto)).thenReturn(team);

    ProjectTeam result = projectTeamMapper.toEntity(projectTeamDto);

    assertEquals(project, result.getProject());
    assertEquals(team, result.getTeam());
  }

  @Test
  void shouldHandleNullProjectTeam() {
    ProjectTeamDto result = projectTeamMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullProjectTeamDto() {
    ProjectTeam result = projectTeamMapper.toEntity(null);
    assertNull(result);
  }
}