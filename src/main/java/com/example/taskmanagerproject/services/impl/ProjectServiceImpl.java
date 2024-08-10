package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_NOT_FOUND_WITH_ID;
import static com.example.taskmanagerproject.utils.MessageUtils.PROJECT_NOT_FOUND_WITH_NAME;

import com.example.taskmanagerproject.dtos.ProjectDto;
import com.example.taskmanagerproject.entities.Project;
import com.example.taskmanagerproject.exceptions.ProjectNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.services.ProjectService;
import com.example.taskmanagerproject.utils.factories.ProjectFactory;
import com.example.taskmanagerproject.utils.mappers.ProjectMapper;
import com.example.taskmanagerproject.utils.validators.ProjectValidator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for handling business logic related to projects in the application.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

  private final ProjectMapper projectMapper;
  private final ProjectFactory projectFactory;
  private final ProjectValidator projectValidator;
  private final ProjectRepository projectRepository;

  @Override
  public ProjectDto createProject(ProjectDto projectDto) {
    projectValidator.validateProjectDto(projectDto);
    Project newProject = projectFactory.createProjectFromRequest(projectDto);
    projectRepository.save(newProject);
    return projectMapper.toDto(newProject);
  }

  @Override
  public ProjectDto getProjectByName(String name) {
    Project existingProject = projectRepository.findByName(name)
        .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + name));
    return projectMapper.toDto(existingProject);
  }

  @Override
  public ProjectDto updateProject(String projectName, ProjectDto projectDto) {
    Project existingProject = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_WITH_NAME + projectName));

    projectValidator.validateProjectDto(existingProject, projectDto);

    existingProject.setName(projectDto.name());
    existingProject.setDescription(projectDto.description());

    projectRepository.save(existingProject);
    return projectMapper.toDto(existingProject);
  }

  @Override
  public void deleteProject(String projectName) {
    Project existingProject = projectRepository.findByName(projectName)
        .orElseThrow(() -> new ProjectNotFoundException(PROJECT_NOT_FOUND_WITH_ID + projectName));

    projectRepository.delete(existingProject);
  }

  @Override
  public List<ProjectDto> getProjectsBySlug(String slug) {
    List<Project> projects = projectRepository.findByCreatorSlug(slug);
    return projects.stream().map(projectMapper::toDto).collect(Collectors.toList());
  }
}
