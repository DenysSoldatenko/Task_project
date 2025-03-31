package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.entities.tasks.TaskPriority.CRITICAL;
import static com.example.taskmanagerproject.entities.tasks.TaskStatus.APPROVED;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.projects.ProjectDto;
import com.example.taskmanagerproject.dtos.tasks.TaskDto;
import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.projects.Project;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.RoleHierarchy;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.ProjectRepository;
import com.example.taskmanagerproject.repositories.RoleHierarchyRepository;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskValidatorTest {

  private TaskValidator taskValidator;
  private UserRepository userRepository;
  private TeamRepository teamRepository;
  private RoleRepository roleRepository;
  private ProjectRepository projectRepository;
  private TeamUserRepository teamUserRepository;
  private RoleHierarchyRepository roleHierarchyRepository;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    teamRepository = mock(TeamRepository.class);
    roleRepository = mock(RoleRepository.class);
    projectRepository = mock(ProjectRepository.class);
    teamUserRepository = mock(TeamUserRepository.class);
    roleHierarchyRepository = mock(RoleHierarchyRepository.class);
    Validator validator = mock(Validator.class);

    taskValidator = new TaskValidator(
      validator,
      userRepository,
      teamRepository,
      roleHierarchyRepository,
      projectRepository,
      roleRepository,
      teamUserRepository
    );
  }

  @Test
  void validateTaskDto_shouldThrow_whenAssignedByHasLowerRole() {
    setupValidCommonEntities();
    when(roleHierarchyRepository.isHigherRoleAssigned(1L, 2L)).thenReturn(false);

    TaskDto dto = validTaskDto();
    ValidationException ex = assertThrows(ValidationException.class, () -> taskValidator.validateTaskDto(dto));
    assertTrue(ex.getMessage().contains("Discrepancy found between roles"));
  }

  @Test
  void validateTaskDto_shouldSucceedWhenAllValidationsPass() {
    setupValidCommonEntities();
    TaskDto dto = validTaskDto();
    assertDoesNotThrow(() -> taskValidator.validateTaskDto(dto));
  }

  @Test
  void validateTaskDto_shouldThrow_whenUserNotInTeam() {
    setupValidCommonEntities();
    when(teamUserRepository.existsByUserIdAndTeamId(anyLong(), anyLong())).thenReturn(false);

    TaskDto dto = validTaskDto();
    assertThrows(ValidationException.class, () -> taskValidator.validateTaskDto(dto));
  }

  @Test
  void validateTaskDto_shouldThrow_whenMissingRoleAssignment() {
    setupValidCommonEntities();
    when(roleRepository.getRoleForUserInTeam(anyLong(), anyLong())).thenReturn(null);

    TaskDto dto = validTaskDto();
    assertThrows(ValidationException.class, () -> taskValidator.validateTaskDto(dto));
  }

  @Test
  void validateTaskDto_shouldThrow_whenUserNotFound() {
    when(projectRepository.findByName("New Project")).thenReturn(Optional.of(new Project()));
    when(teamRepository.findByName("New Team")).thenReturn(Optional.of(new Team()));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.empty());

    TaskDto dto = validTaskDto();
    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> taskValidator.validateTaskDto(dto));
    assertTrue(ex.getMessage().contains("User not found"));
  }

  @Test
  void validateTaskDto_shouldThrow_whenTeamNotFound() {
    when(projectRepository.findByName("New Project")).thenReturn(Optional.of(new Project()));
    when(teamRepository.findByName("New Team")).thenReturn(Optional.empty());

    TaskDto dto = validTaskDto();
    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> taskValidator.validateTaskDto(dto));
    assertTrue(ex.getMessage().contains("Team not found"));
  }

  @Test
  void validateTaskDto_shouldThrow_whenProjectNotFound() {
    when(projectRepository.findByName("New Project")).thenReturn(Optional.empty());

    TaskDto dto = validTaskDto();
    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> taskValidator.validateTaskDto(dto));
    assertTrue(ex.getMessage().contains("Project not found"));
  }

  private TaskDto validTaskDto() {
    UserDto sender = new UserDto(100L, "Sender", "sender@gmail.com", "sender-slug", "", List.of(""));
    UserDto receiver = new UserDto(200L, "Receiver", "receiver@gmail.com", "receiver-slug", "", List.of(""));
    ProjectDto projectDto = new ProjectDto(100L, "New Project", "Description", sender);
    TeamDto teamDto = new TeamDto(100L, "New Team", "Description", sender);
    return new TaskDto(1L, projectDto, teamDto, "Task", "Description", now(), now().plusDays(5), now().plusDays(3), APPROVED, CRITICAL, receiver, sender, null);
  }

  private void setupValidCommonEntities() {
    User sender = new User();
    sender.setId(100L);
    sender.setFullName("Sender");
    sender.setUsername("sender@gmail.com");
    sender.setSlug("sender-slug");

    User receiver = new User();
    receiver.setId(200L);
    receiver.setFullName("Receiver");
    receiver.setUsername("receiver@gmail.com");
    receiver.setSlug("receiver-slug");

    Team team = new Team();
    team.setId(100L);
    team.setName("New Team");
    team.setDescription("Description");
    team.setCreator(sender);

    Project project = new Project();
    project.setId(100L);
    project.setName("New Project");
    project.setDescription("Description");
    project.setCreator(sender);

    Role senderRole = new Role();
    senderRole.setId(1L);
    senderRole.setName("MANAGER");

    Role receiverRole = new Role();
    receiverRole.setId(2L);
    receiverRole.setName("DEVELOPER");

    RoleHierarchy rh = new RoleHierarchy();
    rh.setHigherRole(senderRole);
    rh.setLowerRole(receiverRole);

    TeamUser senderTeamUser = new TeamUser();
    senderTeamUser.setId(new TeamUserId(sender.getId(), team.getId()));
    senderTeamUser.setUser(sender);
    senderTeamUser.setTeam(team);
    senderTeamUser.setRole(senderRole);

    TeamUser receiverTeamUser = new TeamUser();
    receiverTeamUser.setId(new TeamUserId(receiver.getId(), team.getId()));
    receiverTeamUser.setUser(receiver);
    receiverTeamUser.setTeam(team);
    receiverTeamUser.setRole(receiverRole);

    when(userRepository.findByUsername("sender@gmail.com")).thenReturn(Optional.of(sender));
    when(userRepository.findByUsername("receiver@gmail.com")).thenReturn(Optional.of(receiver));
    when(userRepository.findBySlug("sender-slug")).thenReturn(Optional.of(sender));
    when(userRepository.findBySlug("receiver-slug")).thenReturn(Optional.of(receiver));

    when(projectRepository.findByName("New Project")).thenReturn(Optional.of(project));
    when(projectRepository.findByUserSlug("sender-slug")).thenReturn(List.of(project));
    when(projectRepository.findByUserSlug("receiver-slug")).thenReturn(List.of(project));

    when(teamRepository.findByName("New Team")).thenReturn(Optional.of(team));

    when(teamUserRepository.existsByUserIdAndTeamId(100L, 100L)).thenReturn(true);
    when(teamUserRepository.existsByUserIdAndTeamId(200L, 100L)).thenReturn(true);

    when(roleRepository.getRoleForUserInTeam(100L, 100L)).thenReturn(senderRole);
    when(roleRepository.getRoleForUserInTeam(200L, 100L)).thenReturn(receiverRole);

    when(roleHierarchyRepository.isHigherRoleAssigned(1L, 2L)).thenReturn(true);
  }
}
