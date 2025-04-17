package com.example.taskmanagerproject.utils.mappers;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.MEDIUM;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.tasks.Task;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TaskMapperTest {

  @Mock
  private ProjectMapper projectMapper;

  @Mock
  private TeamMapper teamMapper;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private TaskMapperImpl taskMapper;

  private Team team;
  private Project project;
  private User assignedTo;
  private User assignedBy;

  private TeamDto teamDto;
  private ProjectDto projectDto;
  private UserDto assignedToDto;
  private UserDto assignedByDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    project = new Project();
    project.setId(1L);
    team = new Team();
    team.setId(1L);
    assignedTo = new User();
    assignedTo.setId(1L);
    assignedBy = new User();
    assignedBy.setId(2L);

    projectDto = new ProjectDto(1L, "Test Project", "Description", null);
    teamDto = new TeamDto(1L, "Test Team", "Description", null);
    assignedToDto = new UserDto(1L, "user1@gmail.com", "User1", "user1-slug", "", null);
    assignedByDto = new UserDto(2L, "user2@gmail.com", "User2", "user2-slug", "", null);
  }

  @Test
  void shouldMapEntityToDto() {
    Task task = new Task();
    task.setId(1L);
    task.setTitle("Test Task");
    task.setDescription("Description");
    task.setPriority(MEDIUM);
    task.setTaskStatus(APPROVED);
    task.setProject(project);
    task.setTeam(team);
    task.setAssignedTo(assignedTo);
    task.setAssignedBy(assignedBy);
    task.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
    task.setExpirationDate(LocalDateTime.of(2025, 1, 2, 0, 0));

    when(projectMapper.toDto(project)).thenReturn(projectDto);
    when(teamMapper.toDto(team)).thenReturn(teamDto);
    when(userMapper.toDto(assignedTo)).thenReturn(assignedToDto);
    when(userMapper.toDto(assignedBy)).thenReturn(assignedByDto);

    TaskDto result = taskMapper.toDto(task);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Test Task", result.title());
    assertEquals("Description", result.description());
    assertEquals(projectDto, result.project());
    assertEquals(teamDto, result.team());
    assertEquals(assignedToDto, result.assignedTo());
    assertEquals(assignedByDto, result.assignedBy());
    assertEquals(MEDIUM, result.priority());
    assertEquals(APPROVED, result.taskStatus());
    assertNotNull(result.createdAt());
    assertEquals(LocalDateTime.of(2025, 1, 2, 0, 0), result.expirationDate());
  }

  @Test
  void shouldMapDtoToEntity() {
    LocalDateTime now = LocalDateTime.now();
    TaskDto dto = new TaskDto(
        2L,
        projectDto,
        teamDto,
        "New Task",
        "New Desc",
        now,
        now.plusDays(5),
        null,
        APPROVED,
        MEDIUM,
        assignedToDto,
        assignedByDto,
        List.of("img1.png")
    );

    when(projectMapper.toEntity(projectDto)).thenReturn(project);
    when(teamMapper.toEntity(teamDto)).thenReturn(team);
    when(userMapper.toEntity(assignedToDto)).thenReturn(assignedTo);
    when(userMapper.toEntity(assignedByDto)).thenReturn(assignedBy);

    Task result = taskMapper.toEntity(dto);

    assertNotNull(result);
    assertEquals(2L, result.getId());
    assertEquals("New Task", result.getTitle());
    assertEquals("New Desc", result.getDescription());
    assertEquals(project, result.getProject());
    assertEquals(team, result.getTeam());
    assertEquals(assignedTo, result.getAssignedTo());
    assertEquals(assignedBy, result.getAssignedBy());
    assertEquals(MEDIUM, result.getPriority());
    assertEquals(APPROVED, result.getTaskStatus());
    assertEquals(now, result.getCreatedAt());
    assertEquals(now.plusDays(5), result.getExpirationDate());
  }

  @Test
  void shouldReturnNullWhenMappingNullEntity() {
    assertNull(taskMapper.toDto(null));
  }

  @Test
  void shouldReturnNullWhenMappingNullDto() {
    assertNull(taskMapper.toEntity(null));
  }
}
