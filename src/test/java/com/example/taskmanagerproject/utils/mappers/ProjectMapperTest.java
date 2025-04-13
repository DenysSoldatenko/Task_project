package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProjectMapperTest {

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private ProjectMapperImpl projectMapper;

  private Project project;
  private ProjectDto projectDto;

  private User creator;
  private UserDto creatorDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    creator = new User();
    creator.setId(1L);
    creator.setUsername("creator@gmail.com");

    creatorDto = new UserDto(1L, "creator@gmail.com", "Creator", "creator-slug", "", null);

    project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setDescription("Description");
    project.setCreator(creator);

    projectDto = new ProjectDto(1L, "Test Project", "Description", creatorDto);
  }

  @Test
  void shouldMapProjectToDto() {
    when(userMapper.toDto(creator)).thenReturn(creatorDto);

    ProjectDto result = projectMapper.toDto(project);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Test Project", result.name());
    assertEquals("Description", result.description());
    assertEquals(creatorDto, result.creator());
  }

  @Test
  void shouldMapProjectDtoToEntity() {
    when(userMapper.toEntity(creatorDto)).thenReturn(creator);

    Project result = projectMapper.toEntity(projectDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Project", result.getName());
    assertEquals("Description", result.getDescription());
    assertEquals(creator, result.getCreator());
  }

  @Test
  void shouldHandleNullProject() {
    ProjectDto result = projectMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullProjectDto() {
    Project result = projectMapper.toEntity(null);
    assertNull(result);
  }
}