package com.example.taskmanagerproject.utils.validators;

import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_ALREADY_EXISTS;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamValidatorTest {

  private Validator javaxValidator;
  private TeamValidator teamValidator;
  private UserRepository userRepository;
  private TeamRepository teamRepository;
  private RoleRepository roleRepository;
  private TeamUserRepository teamUserRepository;

  @BeforeEach
  void setUp() {
    javaxValidator = mock(Validator.class);
    userRepository = mock(UserRepository.class);
    teamRepository = mock(TeamRepository.class);
    roleRepository = mock(RoleRepository.class);
    teamUserRepository = mock(TeamUserRepository.class);
    teamValidator = new TeamValidator(javaxValidator, userRepository, teamRepository);
  }

  private UserDto adminUserDto() {
    return new UserDto(1L, "Admin User", "admin@example.com", "admin-slug", "", List.of("ADMIN"));
  }

  private void setupAdminTeamUser() {
    User admin = new User();
    admin.setId(1L);
    admin.setUsername("admin@example.com");
    admin.setFullName("Admin User");
    admin.setSlug("admin-slug");

    Team team = new Team();
    team.setId(1L);
    team.setName("Team A");
    team.setCreator(admin);

    Role adminRole = new Role();
    adminRole.setId(1L);
    adminRole.setName("ADMIN");

    TeamUser adminTeamUser = new TeamUser();
    adminTeamUser.setId(new TeamUserId(admin.getId(), team.getId()));
    adminTeamUser.setUser(admin);
    adminTeamUser.setTeam(team);
    adminTeamUser.setRole(adminRole);

    when(userRepository.findByUsername("admin@example.com")).thenReturn(Optional.of(admin));
    when(teamUserRepository.existsByUserIdAndTeamId(admin.getId(), team.getId())).thenReturn(true);
    when(roleRepository.getRoleForUserInTeam(admin.getId(), team.getId())).thenReturn(adminRole);
    when(userRepository.isUserInLeadershipPosition("admin@example.com")).thenReturn(true);
  }

  @Test
  void shouldPassValidationForNewTeamWithAdminUser() {
    TeamDto dto = new TeamDto(1L, "Team A", "Valid team", adminUserDto());

    setupAdminTeamUser();
    when(teamRepository.existsByName("Team A")).thenReturn(false);
    when(javaxValidator.validate(dto)).thenReturn(Collections.emptySet());

    assertDoesNotThrow(() -> teamValidator.validateTeamDto(dto));
  }

  @Test
  void shouldThrowIfTeamNameAlreadyExists() {
    TeamDto dto = new TeamDto(1L, "Team A", "Some desc", adminUserDto());

    setupAdminTeamUser();
    when(teamRepository.existsByName("Team A")).thenReturn(true);

    ValidationException ex = assertThrows(ValidationException.class, () -> teamValidator.validateTeamDto(dto));
    assertTrue(ex.getMessage().contains(TEAM_ALREADY_EXISTS + "Team A"));
  }

  @Test
  void shouldThrowIfUserNotLeaderWhenCreatingTeam() {
    TeamDto dto = new TeamDto(1L, "Team A", "Some desc", new UserDto(1L, "john", "john@example.com", "slug", "", null));
    when(teamRepository.existsByName("Team A")).thenReturn(false);
    when(userRepository.isUserInLeadershipPosition("john")).thenReturn(false);

    ValidationException ex = assertThrows(ValidationException.class, () -> teamValidator.validateTeamDto(dto));
    assertTrue(ex.getMessage().contains(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM + "john"));
  }

  @Test
  void shouldThrowIfUserNotCreatorNorLeaderWhenUpdatingTeam() {
    Team existing = new Team();
    existing.setName("Team A");

    when(teamRepository.existsByName("Team A")).thenReturn(false);
    when(userRepository.isTeamCreator("Team A", "john")).thenReturn(false);
    when(userRepository.isUserInLeadershipPositionInTeam("Team A", "john")).thenReturn(false);
    TeamDto dto = new TeamDto(1L, "Team A", "Some desc", new UserDto(1L, "john", "john@example.com", "slug", "", null));

    ValidationException ex = assertThrows(ValidationException.class, () -> teamValidator.validateTeamDto(dto, existing));
    assertTrue(ex.getMessage().contains(USER_DOES_NOT_HAVE_ROLE_TO_CREATE_OR_UPDATE_TEAM + "john"));
  }

  @Test
  void shouldNotThrowWhenUpdatingWithSameNameAndUserIsCreator() {
    Team existing = new Team();
    existing.setName("Team A");

    when(teamRepository.existsByName("Team A")).thenReturn(false);
    when(userRepository.isTeamCreator("Team A", "john@example.com")).thenReturn(true);
    TeamDto dto = new TeamDto(1L, "Team A", "desc", new UserDto(1L, "john", "john@example.com", "slug", "", null));

    assertDoesNotThrow(() -> teamValidator.validateTeamDto(dto, existing));
  }

  @Test
  void shouldCollectConstraintViolations() {

    ConstraintViolation<TeamDto> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("Team name must not be blank");

    TeamDto dto = new TeamDto(null, "", "", adminUserDto());
    setupAdminTeamUser();
    when(javaxValidator.validate(dto)).thenReturn(singleton(violation));

    ValidationException ex = assertThrows(ValidationException.class, () -> teamValidator.validateTeamDto(dto));
    assertTrue(ex.getMessage().contains("Team name must not be blank"));
  }
}
