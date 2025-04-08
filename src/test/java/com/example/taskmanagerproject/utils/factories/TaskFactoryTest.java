package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskFactoryTest {

  private TaskFactory taskFactory;
  private ProjectRepository projectRepository;
  private TeamRepository teamRepository;
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    projectRepository = mock(ProjectRepository.class);
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class);
    taskFactory = new TaskFactory(teamRepository, userRepository, projectRepository);
  }

  @Test
  void shouldCreateTaskSuccessfully() {
    UserDto senderDto = new UserDto(1L, "sender", "sender@example.com", "sender-slug", "", null);
    UserDto receiverDto = new UserDto(2L, "receiver", "receiver@example.com", "receiver-slug", "", null);
    ProjectDto projectDto = new ProjectDto(1L, "P", "desc", senderDto);
    TeamDto teamDto = new TeamDto(1L, "T", "desc", senderDto);
    LocalDateTime future = now().plusDays(1);

    TaskDto dto = new TaskDto(null, projectDto, teamDto, "T", "D", null, future, null, APPROVED, CRITICAL, receiverDto, senderDto, null);

    Project project = new Project();
    Team team = new Team();
    User sender = new User();
    User receiver = new User();

    when(projectRepository.findByName("P")).thenReturn(Optional.of(project));
    when(teamRepository.findByName("T")).thenReturn(Optional.of(team));
    when(userRepository.findByUsername("receiver@example.com")).thenReturn(Optional.of(receiver));
    when(userRepository.findByUsername("sender@example.com")).thenReturn(Optional.of(sender));

    Task task = taskFactory.createTaskFromDto(dto);

    assertNotNull(task);
    assertEquals("T", task.getTitle());
    assertEquals(CRITICAL, task.getPriority());
    assertEquals(APPROVED, task.getTaskStatus());
    assertEquals(receiver, task.getAssignedTo());
    assertEquals(sender, task.getAssignedBy());
  }

  @Test
  void shouldThrowIfProjectNotFound() {
    when(projectRepository.findByName("Unknown")).thenReturn(Optional.empty());
    TaskDto dto = new TaskDto(null, new ProjectDto(null, "Unknown", null, null), null, "T", "", null, now().plusDays(1), null, APPROVED, CRITICAL, null, null, null);
    assertThrows(ResourceNotFoundException.class, () -> taskFactory.createTaskFromDto(dto));
  }

  @Test
  void shouldThrowIfTeamNotFound() {
    when(projectRepository.findByName("P")).thenReturn(Optional.of(new Project()));
    when(teamRepository.findByName("Missing")).thenReturn(Optional.empty());
    TaskDto dto = new TaskDto(
        null,
        new ProjectDto(null, "P", null, null),
        new TeamDto(null, "Missing", "", null),
        "T", "", null, now().plusDays(1), null, APPROVED, CRITICAL, null, null, null
    );
    assertThrows(ResourceNotFoundException.class, () -> taskFactory.createTaskFromDto(dto));
  }

  @Test
  void shouldThrowIfExpirationDateIsInPast() {
    when(projectRepository.findByName("P")).thenReturn(Optional.of(new Project()));
    when(teamRepository.findByName("T")).thenReturn(Optional.of(new Team()));
    when(userRepository.findByUsername("receiver")).thenReturn(Optional.of(new User()));
    when(userRepository.findByUsername("sender")).thenReturn(Optional.of(new User()));

    TaskDto dto = new TaskDto(
        null,
        new ProjectDto(null, "P", "", null),
        new TeamDto(null, "T", "", null), "title", "desc", null, now().minusDays(1), null, APPROVED, CRITICAL,
        new UserDto(1L, "receiver", "", "", "", null),
        new UserDto(2L, "sender", "", "", "", null), null
    );

    assertThrows(ResourceNotFoundException.class, () -> taskFactory.createTaskFromDto(dto));
  }
}
