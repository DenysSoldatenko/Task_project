package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProjectFactoryTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ProjectFactory projectFactory;

  private ProjectDto projectDto;
  private User user;

  @BeforeEach
  void setUp() {
    UserDto creatorDto = new UserDto(1L, "Test User", "user@gmail.com", "user-slug", "", List.of(""));
    projectDto = new ProjectDto(1L, "Test Project", "Description", creatorDto);

    user = new User();
    user.setId(1L);
    user.setUsername("user@gmail.com");
  }

  @Test
  void createProjectFromRequest_shouldCreateProject() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));

    Project project = projectFactory.createProjectFromRequest(projectDto);

    assertEquals("Test Project", project.getName());
    assertEquals("Description", project.getDescription());
    assertEquals(user, project.getCreator());
    assertNotNull(project.getCreatedAt());
  }

  @Test
  void createProjectFromRequest_shouldThrowIfUserNotFound() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> projectFactory.createProjectFromRequest(projectDto));

    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "user@gmail.com", ex.getMessage());
  }

  @Test
  void createProjectFromRequest_shouldHandleNullDescription() {
    ProjectDto dtoWithNullDescription = new ProjectDto(2L, "No Description Project", null, projectDto.creator());
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));

    Project project = projectFactory.createProjectFromRequest(dtoWithNullDescription);

    assertEquals("No Description Project", project.getName());
    assertNull(project.getDescription());
    assertEquals(user, project.getCreator());
    assertNotNull(project.getCreatedAt());
  }

  @Test
  void createProjectFromRequest_shouldHandleEmptyProjectName() {
    ProjectDto emptyNameDto = new ProjectDto(4L, "", "Empty name", projectDto.creator());
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));

    Project project = projectFactory.createProjectFromRequest(emptyNameDto);

    assertEquals("", project.getName());
    assertEquals("Empty name", project.getDescription());
    assertEquals(user, project.getCreator());
    assertNotNull(project.getCreatedAt());
  }

  @Test
  void createProjectFromRequest_shouldHandleNullProjectName() {
    ProjectDto nullNameDto = new ProjectDto(5L, null, "No name project", projectDto.creator());
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));

    Project project = projectFactory.createProjectFromRequest(nullNameDto);

    assertNull(project.getName());
    assertEquals("No name project", project.getDescription());
    assertEquals(user, project.getCreator());
    assertNotNull(project.getCreatedAt());
  }
}
