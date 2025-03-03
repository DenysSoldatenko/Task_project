package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.ProjectTeamRepository;
import com.example.taskmanagerproject.utils.factories.ProjectFactory;
import com.example.taskmanagerproject.utils.factories.ProjectTeamFactory;
import com.example.taskmanagerproject.utils.mappers.ProjectMapper;
import com.example.taskmanagerproject.utils.mappers.ProjectTeamMapper;
import com.example.taskmanagerproject.utils.validators.ProjectValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

class ProjectServiceImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ProjectTeamRepository projectTeamRepository;

  @Mock
  private ProjectMapper projectMapper;

  @Mock
  private ProjectTeamMapper projectTeamMapper;

  @Mock
  private ProjectFactory projectFactory;

  @Mock
  private ProjectTeamFactory projectTeamFactory;

  @Mock
  private ProjectValidator projectValidator;

  @InjectMocks
  private ProjectServiceImpl projectService;

  private Project project;
  private ProjectDto projectDto;
  private ProjectTeam projectTeam;
  private ProjectTeamDto projectTeamDto;

  private final String projectName = "TestProject";
  private final String newProjectName = "TestProject 2";
  private final String teamName = "TestTeam";
  private final String slug = "test-slug";
  private final String username = "testuser";
  private final Long userId = 1L;
  private final Long projectId = 1L;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    project = mock(Project.class);
    projectDto = mock(ProjectDto.class);
    projectTeam = mock(ProjectTeam.class);
    projectTeamDto = mock(ProjectTeamDto.class);

    when(projectDto.name()).thenReturn(projectName);
    when(projectDto.description()).thenReturn("Test Description");
    when(projectMapper.toDto(any(Project.class))).thenReturn(projectDto);
    when(projectTeamMapper.toDto(any(ProjectTeam.class))).thenReturn(projectTeamDto);
  }

  @Test
  void createProject_shouldCreateAndReturnProjectDto() {
    doNothing().when(projectValidator).validateProjectDto(projectDto);
    when(projectFactory.createProjectFromRequest(projectDto)).thenReturn(project);
    when(projectRepository.save(project)).thenReturn(project);
    when(projectMapper.toDto(project)).thenReturn(projectDto);

    ProjectDto result = projectService.createProject(projectDto);

    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectValidator).validateProjectDto(projectDto);
    verify(projectFactory).createProjectFromRequest(projectDto);
    verify(projectRepository).save(project);
    verify(projectMapper).toDto(project);
  }

  @Test
  void createProject_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(projectDto.name()).thenReturn("Invalid@Name");
    doThrow(new IllegalArgumentException("Invalid project name format")).when(projectValidator).validateProjectDto(projectDto);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> projectService.createProject(projectDto));
    assertEquals("Invalid project name format", exception.getMessage());
    verify(projectValidator).validateProjectDto(projectDto);
  }

  @Test
  void createProject_shouldThrowDataIntegrityViolationExceptionWhenNameExists() {
    doNothing().when(projectValidator).validateProjectDto(projectDto);
    when(projectFactory.createProjectFromRequest(projectDto)).thenReturn(project);
    doThrow(new DataIntegrityViolationException("Duplicate project name")).when(projectRepository).save(project);

    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> projectService.createProject(projectDto));
    assertEquals("Duplicate project name", exception.getMessage());
    verify(projectValidator).validateProjectDto(projectDto);
    verify(projectFactory).createProjectFromRequest(projectDto);
    verify(projectRepository).save(project);
  }

  @Test
  void createProject_shouldThrowIllegalArgumentExceptionWhenDtoIsNull() {
    doThrow(new IllegalArgumentException("Project DTO cannot be null")).when(projectValidator).validateProjectDto(null);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> projectService.createProject(null));
    assertEquals("Project DTO cannot be null", exception.getMessage());
    verify(projectValidator).validateProjectDto(null);
  }

  @Test
  void createProject_shouldHandleLargeDescription() {
    String largeDescription = "A".repeat(1000); // 1000 chars
    when(projectDto.description()).thenReturn(largeDescription);
    doNothing().when(projectValidator).validateProjectDto(projectDto);
    when(projectFactory.createProjectFromRequest(projectDto)).thenReturn(project);
    when(projectRepository.save(project)).thenReturn(project);
    when(projectMapper.toDto(project)).thenReturn(projectDto);

    ProjectDto result = projectService.createProject(projectDto);

    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectValidator).validateProjectDto(projectDto);
    verify(projectFactory).createProjectFromRequest(projectDto);
    verify(projectRepository).save(project);
    verify(projectMapper).toDto(project);
  }

  @Test
  void getProjectByName_shouldReturnProjectDtoWhenProjectExists() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    ProjectDto result = projectService.getProjectByName(projectName);
    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectRepository).findByName(projectName);
    verify(projectMapper).toDto(project);
  }

  @Test
  void getProjectByName_shouldThrowResourceNotFoundExceptionWhenProjectNotFound() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectByName(projectName));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + projectName, exception.getMessage());
    verify(projectRepository).findByName(projectName);
  }

  @Test
  void getProjectByName_shouldThrowResourceNotFoundExceptionWhenNameIsEmpty() {
    String emptyName = "";
    when(projectRepository.findByName(emptyName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectByName(emptyName));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME, exception.getMessage());
    verify(projectRepository).findByName(emptyName);
  }

  @Test
  void getProjectByName_shouldHandleSpecialCharactersInName() {
    String specialName = "Project#2025!";
    when(projectRepository.findByName(specialName)).thenReturn(Optional.of(project));
    ProjectDto result = projectService.getProjectByName(specialName);
    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectRepository).findByName(specialName);
    verify(projectMapper).toDto(project);
  }

  @Test
  void getTeamsForProject_shouldReturnTeamDtosWhenTeamsExist() {
    when(projectTeamRepository.findAllByProjectName(projectName)).thenReturn(List.of(projectTeam));
    List<ProjectTeamDto> result = projectService.getTeamsForProject(projectName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectTeamDto, result.get(0));
    verify(projectTeamRepository).findAllByProjectName(projectName);
    verify(projectTeamMapper).toDto(projectTeam);
  }

  @Test
  void getTeamsForProject_shouldReturnEmptyListWhenNoTeams() {
    when(projectTeamRepository.findAllByProjectName(projectName)).thenReturn(Collections.emptyList());
    List<ProjectTeamDto> result = projectService.getTeamsForProject(projectName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectTeamRepository).findAllByProjectName(projectName);
  }

  @Test
  void getTeamsForProject_shouldHandleEmptyProjectName() {
    when(projectTeamRepository.findAllByProjectName("")).thenReturn(Collections.emptyList());
    List<ProjectTeamDto> result = projectService.getTeamsForProject("");
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectTeamRepository).findAllByProjectName("");
  }

  @Test
  void getTeamsForProject_shouldHandleSpecialCharactersInProjectName() {
    String specialName = "Project#2025!";
    when(projectTeamRepository.findAllByProjectName(specialName)).thenReturn(List.of(projectTeam));
    List<ProjectTeamDto> result = projectService.getTeamsForProject(specialName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectTeamDto, result.get(0));
    verify(projectTeamRepository).findAllByProjectName(specialName);
    verify(projectTeamMapper).toDto(projectTeam);
  }

  @Test
  void getProjectsForTeam_shouldReturnProjectTeamDtosWhenProjectsExist() {
    when(projectTeamRepository.findAllByTeamName(teamName)).thenReturn(List.of(projectTeam));
    List<ProjectTeamDto> result = projectService.getProjectsForTeam(teamName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectTeamDto, result.get(0));
    verify(projectTeamRepository).findAllByTeamName(teamName);
    verify(projectTeamMapper).toDto(projectTeam);
  }

  @Test
  void getProjectsForTeam_shouldReturnEmptyListWhenNoProjects() {
    when(projectTeamRepository.findAllByTeamName(teamName)).thenReturn(Collections.emptyList());
    List<ProjectTeamDto> result = projectService.getProjectsForTeam(teamName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectTeamRepository).findAllByTeamName(teamName);
  }

  @Test
  void getProjectsForTeam_shouldHandleEmptyTeamName() {
    when(projectTeamRepository.findAllByTeamName("")).thenReturn(Collections.emptyList());
    List<ProjectTeamDto> result = projectService.getProjectsForTeam("");
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectTeamRepository).findAllByTeamName("");
  }

  @Test
  void getProjectsForTeam_shouldHandleSpecialCharactersInTeamName() {
    String specialName = "Team@2025!";
    when(projectTeamRepository.findAllByTeamName(specialName)).thenReturn(List.of(projectTeam));
    List<ProjectTeamDto> result = projectService.getProjectsForTeam(specialName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectTeamDto, result.get(0));
    verify(projectTeamRepository).findAllByTeamName(specialName);
    verify(projectTeamMapper).toDto(projectTeam);
  }

  @Test
  void getRoleByProjectNameAndUsername_shouldReturnRoleWhenExists() {
    Role role = new Role();
    role.setName("ADMIN");
    when(projectRepository.findRoleByProjectNameAndUsername(projectName, username)).thenReturn(role);
    Role result = projectService.getRoleByProjectNameAndUsername(projectName, username);
    assertEquals(role, result);
    verify(projectRepository).findRoleByProjectNameAndUsername(projectName, username);
  }

  @Test
  void getRoleByProjectNameAndUsername_shouldReturnNullWhenRoleNotFound() {
    when(projectRepository.findRoleByProjectNameAndUsername(projectName, username)).thenReturn(null);
    Role result = projectService.getRoleByProjectNameAndUsername(projectName, username);
    assertNull(result);
    verify(projectRepository).findRoleByProjectNameAndUsername(projectName, username);
  }

  @Test
  void getRoleByProjectNameAndUsername_shouldHandleEmptyInputs() {
    when(projectRepository.findRoleByProjectNameAndUsername("", "")).thenReturn(null);
    Role result = projectService.getRoleByProjectNameAndUsername("", "");
    assertNull(result);
    verify(projectRepository).findRoleByProjectNameAndUsername("", "");
  }

  @Test
  void getRoleByProjectNameAndUsername_shouldHandleSpecialCharacters() {
    String specialName = "Project#2025!";
    String specialUser = "user@2025";
    Role role = new Role();
    role.setName("MEMBER");
    when(projectRepository.findRoleByProjectNameAndUsername(specialName, specialUser)).thenReturn(role);
    Role result = projectService.getRoleByProjectNameAndUsername(specialName, specialUser);
    assertEquals(role, result);
    verify(projectRepository).findRoleByProjectNameAndUsername(specialName, specialUser);
  }

  @Test
  void existsByUserIdAndProjectId_shouldReturnTrueWhenAssociationExists() {
    when(projectRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);
    boolean result = projectService.existsByUserIdAndProjectId(userId, projectId);
    assertTrue(result);
    verify(projectRepository).existsByUserIdAndProjectId(userId, projectId);
  }

  @Test
  void existsByUserIdAndProjectId_shouldReturnFalseWhenAssociationNotExists() {
    when(projectRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(false);
    boolean result = projectService.existsByUserIdAndProjectId(userId, projectId);
    assertFalse(result);
    verify(projectRepository).existsByUserIdAndProjectId(userId, projectId);
  }

  @Test
  void existsByUserIdAndProjectId_shouldHandleZeroIds() {
    when(projectRepository.existsByUserIdAndProjectId(0L, 0L)).thenReturn(false);
    boolean result = projectService.existsByUserIdAndProjectId(0L, 0L);
    assertFalse(result);
    verify(projectRepository).existsByUserIdAndProjectId(0L, 0L);
  }

  @Test
  void existsByUserIdAndProjectId_shouldHandleNegativeIds() {
    when(projectRepository.existsByUserIdAndProjectId(-1L, -1L)).thenReturn(false);
    boolean result = projectService.existsByUserIdAndProjectId(-1L, -1L);
    assertFalse(result);
    verify(projectRepository).existsByUserIdAndProjectId(-1L, -1L);
  }

  @Test
  void getProjectsBySlug_shouldReturnProjectDtosWhenProjectsExist() {
    when(projectRepository.findByUserSlug(slug)).thenReturn(List.of(project));
    List<ProjectDto> result = projectService.getProjectsBySlug(slug);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectDto, result.get(0));
    verify(projectRepository).findByUserSlug(slug);
    verify(projectMapper).toDto(project);
  }

  @Test
  void getProjectsBySlug_shouldReturnEmptyListWhenNoProjects() {
    when(projectRepository.findByUserSlug(slug)).thenReturn(Collections.emptyList());
    List<ProjectDto> result = projectService.getProjectsBySlug(slug);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectRepository).findByUserSlug(slug);
  }

  @Test
  void getProjectsBySlug_shouldHandleEmptySlug() {
    when(projectRepository.findByUserSlug("")).thenReturn(Collections.emptyList());
    List<ProjectDto> result = projectService.getProjectsBySlug("");
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(projectRepository).findByUserSlug("");
  }

  @Test
  void getProjectsBySlug_shouldHandleSpecialCharactersInSlug() {
    String specialSlug = "slug#2025!";
    when(projectRepository.findByUserSlug(specialSlug)).thenReturn(List.of(project));
    List<ProjectDto> result = projectService.getProjectsBySlug(specialSlug);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(projectDto, result.get(0));
    verify(projectRepository).findByUserSlug(specialSlug);
    verify(projectMapper).toDto(project);
  }

  @Test
  void updateProject_shouldUpdateAndReturnProjectDto() {
    when(projectDto.name()).thenReturn(newProjectName);
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    doNothing().when(projectValidator).validateProjectDto(projectDto, project);
    when(projectRepository.save(project)).thenReturn(project);
    when(projectMapper.toDto(project)).thenReturn(projectDto);

    ProjectDto result = projectService.updateProject(projectName, projectDto);
    assertNotNull(result);
    assertEquals(projectDto, result);

    verify(projectRepository).findByName(projectName);
    verify(projectValidator).validateProjectDto(projectDto, project);
    verify(project).setName(newProjectName);
    verify(projectRepository).save(project);
    verify(projectMapper).toDto(project);
  }

  @Test
  void updateProject_shouldThrowResourceNotFoundExceptionWhenProjectNotFound() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(projectName, projectDto));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + projectName, exception.getMessage());
    verify(projectRepository).findByName(projectName);
  }

  @Test
  void updateProject_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    doThrow(new IllegalArgumentException("Invalid project name")).when(projectValidator).validateProjectDto(projectDto, project);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> projectService.updateProject(projectName, projectDto));
    assertEquals("Invalid project name", exception.getMessage());
    verify(projectRepository).findByName(projectName);
    verify(projectValidator).validateProjectDto(projectDto, project);
  }

  @Test
  void updateProject_shouldHandleEmptyProjectName() {
    when(projectRepository.findByName("")).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject("", projectDto));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME, exception.getMessage());
    verify(projectRepository).findByName("");
  }

  @Test
  void deleteProject_shouldDeleteProjectWhenExists() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    doNothing().when(projectRepository).delete(project);
    projectService.deleteProject(projectName);
    verify(projectRepository).findByName(projectName);
    verify(projectRepository).delete(project);
  }

  @Test
  void deleteProject_shouldThrowResourceNotFoundExceptionWhenProjectNotFound() {
    when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(projectName));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + projectName, exception.getMessage());
    verify(projectRepository).findByName(projectName);
  }

  @Test
  void deleteProject_shouldHandleEmptyProjectName() {
    when(projectRepository.findByName("")).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(""));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME, exception.getMessage());
    verify(projectRepository).findByName("");
  }

  @Test
  void addTeamToProject_shouldAddTeamsAndReturnProjectDto() {
    List<ProjectTeamDto> teamDtos = List.of(projectTeamDto);
    List<ProjectTeam> teams = List.of(projectTeam);
    when(projectTeamFactory.createProjectTeamAssociations(teamDtos)).thenReturn(teams);
    when(projectTeamRepository.saveAll(teams)).thenReturn(teams);
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    ProjectDto result = projectService.addTeamToProject(projectName, teamDtos);
    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectTeamFactory).createProjectTeamAssociations(teamDtos);
    verify(projectTeamRepository).saveAll(teams);
    verify(projectRepository).findByName(projectName);
    verify(projectMapper).toDto(project);
  }

  @Test
  void addTeamToProject_shouldThrowResourceNotFoundExceptionWhenProjectNotFound() {
    List<ProjectTeamDto> teamDtos = List.of(projectTeamDto);
    when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> projectService.addTeamToProject(projectName, teamDtos));
    assertEquals(PROJECT_NOT_FOUND_WITH_NAME + projectName, exception.getMessage());
    verify(projectRepository).findByName(projectName);
  }

  @Test
  void addTeamToProject_shouldHandleEmptyTeamList() {
    List<ProjectTeamDto> teamDtos = Collections.emptyList();
    when(projectTeamFactory.createProjectTeamAssociations(teamDtos)).thenReturn(Collections.emptyList());
    when(projectTeamRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());
    when(projectRepository.findByName(projectName)).thenReturn(Optional.of(project));
    ProjectDto result = projectService.addTeamToProject(projectName, teamDtos);
    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectTeamFactory).createProjectTeamAssociations(teamDtos);
    verify(projectTeamRepository).saveAll(Collections.emptyList());
    verify(projectRepository).findByName(projectName);
    verify(projectMapper).toDto(project);
  }

  @Test
  void addTeamToProject_shouldHandleSpecialCharactersInProjectName() {
    String specialName = "Project#2025!";
    List<ProjectTeamDto> teamDtos = List.of(projectTeamDto);
    List<ProjectTeam> teams = List.of(projectTeam);
    when(projectTeamFactory.createProjectTeamAssociations(teamDtos)).thenReturn(teams);
    when(projectTeamRepository.saveAll(teams)).thenReturn(teams);
    when(projectRepository.findByName(specialName)).thenReturn(Optional.of(project));
    ProjectDto result = projectService.addTeamToProject(specialName, teamDtos);
    assertNotNull(result);
    assertEquals(projectDto, result);
    verify(projectTeamFactory).createProjectTeamAssociations(teamDtos);
    verify(projectTeamRepository).saveAll(teams);
    verify(projectRepository).findByName(specialName);
    verify(projectMapper).toDto(project);
  }
}