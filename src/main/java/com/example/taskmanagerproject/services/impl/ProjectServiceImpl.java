package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.PROJECT_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.projects.ProjectTeamDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.projects.ProjectTeam;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.ProjectTeamRepository;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.utils.factories.ProjectFactory;
import com.example.taskmanagerproject.utils.factories.ProjectTeamFactory;
import com.example.taskmanagerproject.utils.mappers.ProjectMapper;
import com.example.taskmanagerproject.utils.mappers.ProjectTeamMapper;
import com.example.taskmanagerproject.utils.validators.ProjectValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ProjectService interface.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

  private final ProjectMapper projectMapper;
  private final ProjectFactory projectFactory;
  private final ProjectValidator projectValidator;
  private final ProjectRepository projectRepository;

  private final ProjectTeamMapper projectTeamMapper;
  private final ProjectTeamFactory projectTeamFactory;
  private final ProjectTeamRepository projectTeamRepository;

  @Override
  @Transactional
  public ProjectDto createProject(ProjectDto projectDto) {
    projectValidator.validateProjectDto(projectDto);
    Project newProject = projectFactory.createProjectFromRequest(projectDto);
    projectRepository.save(newProject);
    return projectMapper.toDto(newProject);
  }

  @Override
  public ProjectDto getProjectByName(String name) {
    Project existingProject = projectRepository.findByName(name)
        .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + name));
    return projectMapper.toDto(existingProject);
  }

  @Override
  public List<ProjectTeamDto> getTeamsForProject(String projectName) {
    return projectTeamRepository.findAllByProjectName(projectName).stream().map(projectTeamMapper::toDto).toList();
  }

  @Override
  public List<ProjectTeamDto> getProjectsForTeam(String teamName) {
    return projectTeamRepository.findAllByTeamName(teamName).stream().map(projectTeamMapper::toDto).toList();
  }

  @Override
  public List<ProjectDto> getProjectsBySlug(String slug) {
    List<Project> projectDtoList = projectRepository.findByUserSlug(slug);
    return projectDtoList.stream().map(projectMapper::toDto).toList();
  }

  @Override
  @Transactional
  public ProjectDto updateProject(String projectName, ProjectDto projectDto) {
    Project existingProject = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectName));

    projectValidator.validateProjectDto(projectDto, existingProject);

    existingProject.setName(projectDto.name());
    existingProject.setDescription(projectDto.description());

    projectRepository.save(existingProject);
    return projectMapper.toDto(existingProject);
  }

  @Override
  @Transactional
  public void deleteProject(String projectName) {
    Project existingProject = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectName));

    projectRepository.delete(existingProject);
  }

  @Override
  @Transactional
  public ProjectDto addTeamToProject(String projectName, List<ProjectTeamDto> projectTeamDtoList) {
    List<ProjectTeam> projectTeamList = projectTeamFactory.createProjectTeamAssociations(projectTeamDtoList);
    projectTeamRepository.saveAll(projectTeamList);
    return getProjectByName(projectName);
  }
}
