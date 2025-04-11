package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.ROLE_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_ALREADY_IN_TEAM;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.dtos.users.RoleDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.entities.teams.TeamUserId;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.example.taskmanagerproject.repositories.RoleRepository;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TeamUserFactoryTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private TeamUserRepository teamUserRepository;

  @InjectMocks
  private TeamUserFactory teamUserFactory;

  private User user;
  private Team team;
  private Role role;
  private TeamUserDto teamUserDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    UserDto userDto = new UserDto(1L, "User", "user@gmail.com", "user-slug", "", List.of(""));
    TeamDto teamDto = new TeamDto(1L, "Test Team", "Description", userDto);
    RoleDto roleDto = new RoleDto(1L, "MEMBER", "Team member role");
    teamUserDto = new TeamUserDto(userDto, teamDto, roleDto);

    user = new User();
    user.setId(1L);
    user.setUsername("user@gmail.com");

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");

    role = new Role();
    role.setName("MEMBER");
  }

  @Test
  void shouldCreateUserTeamAssociations() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));
    when(roleRepository.findByName("MEMBER")).thenReturn(Optional.of(role));
    when(teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(false);

    List<TeamUser> result = teamUserFactory.createUserTeamAssociations(List.of(teamUserDto));

    assertEquals(1, result.size());
    TeamUser teamUser = result.get(0);
    assertEquals(user, teamUser.getUser());
    assertEquals(team, teamUser.getTeam());
    assertEquals(role, teamUser.getRole());
    assertEquals(new TeamUserId(user.getId(), team.getId()), teamUser.getId());
  }

  @Test
  void shouldThrowIfUserNotFound() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> teamUserFactory.createUserTeamAssociations(List.of(teamUserDto)));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "user@gmail.com", ex.getMessage());
  }

  @Test
  void shouldThrowIfTeamNotFound() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> teamUserFactory.createUserTeamAssociations(List.of(teamUserDto)));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + "Test Team", ex.getMessage());
  }

  @Test
  void shouldThrowIfRoleNotFound() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));
    when(roleRepository.findByName("MEMBER")).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> teamUserFactory.createUserTeamAssociations(List.of(teamUserDto)));
    assertEquals(ROLE_NOT_FOUND_WITH_NAME + "MEMBER", ex.getMessage());
  }

  @Test
  void shouldThrowIfUserAlreadyInTeam() {
    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));
    when(roleRepository.findByName("MEMBER")).thenReturn(Optional.of(role));
    when(teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(true);

    ValidationException ex = assertThrows(ValidationException.class, () -> teamUserFactory.createUserTeamAssociations(List.of(teamUserDto)));
    assertEquals(String.format(USER_ALREADY_IN_TEAM, user.getId(), team.getId()), ex.getMessage());
  }

  @Test
  void shouldCreateMultipleUserTeamAssociations() {
    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("user2@gmail.com");

    Team team2 = new Team();
    team2.setId(2L);
    team2.setName("Team2");

    Role role2 = new Role();
    role2.setName("LEADER");

    when(userRepository.findByUsername("user@gmail.com")).thenReturn(Optional.of(user));
    when(teamRepository.findByName("Test Team")).thenReturn(Optional.of(team));
    when(roleRepository.findByName("MEMBER")).thenReturn(Optional.of(role));
    when(teamUserRepository.existsByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(false);
    when(userRepository.findByUsername("user2@gmail.com")).thenReturn(Optional.of(user2));
    when(teamRepository.findByName("Team2")).thenReturn(Optional.of(team2));
    when(roleRepository.findByName("LEADER")).thenReturn(Optional.of(role2));
    when(teamUserRepository.existsByUserIdAndTeamId(user2.getId(), team2.getId())).thenReturn(false);

    UserDto userDto2 = new UserDto(2L, "User2", "user2@gmail.com", "user2-slug", "", List.of(""));
    TeamDto teamDto2 = new TeamDto(2L, "Team2", "Description2", userDto2);
    RoleDto roleDto2 = new RoleDto(1L, "LEADER", "Team leader role");
    TeamUserDto teamUserDto2 = new TeamUserDto(userDto2, teamDto2, roleDto2);
    List<TeamUser> result = teamUserFactory.createUserTeamAssociations(List.of(teamUserDto, teamUserDto2));

    assertEquals(2, result.size());
    assertEquals(user, result.get(0).getUser());
    assertEquals(team, result.get(0).getTeam());
    assertEquals(role, result.get(0).getRole());
    assertEquals(user2, result.get(1).getUser());
    assertEquals(team2, result.get(1).getTeam());
    assertEquals(role2, result.get(1).getRole());
  }
}