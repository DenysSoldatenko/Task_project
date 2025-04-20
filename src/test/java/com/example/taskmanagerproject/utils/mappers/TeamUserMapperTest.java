package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TeamUserMapperTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private TeamMapper teamMapper;

  @Mock
  private RoleMapper roleMapper;

  @InjectMocks
  private TeamUserMapperImpl teamUserMapper;

  private TeamUser teamUser;
  private TeamUserDto teamUserDto;

  private User user;
  private Team team;
  private Role role;

  private UserDto userDto;
  private TeamDto teamDto;
  private RoleDto roleDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = new User();
    user.setId(1L);
    user.setUsername("user@gmail.com");

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");

    role = new Role();
    role.setName("MEMBER");

    teamUser = new TeamUser();
    teamUser.setId(new TeamUserId(user.getId(), team.getId()));
    teamUser.setUser(user);
    teamUser.setTeam(team);
    teamUser.setRole(role);

    userDto = new UserDto(1L, "user@gmail.com", "User", "user-slug", "", null);
    teamDto = new TeamDto(1L, "Test Team", "Description", null);
    roleDto = new RoleDto(100L, "MEMBER", "Team member role");
    teamUserDto = new TeamUserDto(userDto, teamDto, roleDto);
  }

  @Test
  void shouldMapTeamUserToDto() {
    when(userMapper.toDto(user)).thenReturn(userDto);
    when(teamMapper.toDto(team)).thenReturn(teamDto);
    when(roleMapper.toDto(role)).thenReturn(roleDto);

    TeamUserDto result = teamUserMapper.toDto(teamUser);

    assertNotNull(result);
    assertEquals(userDto, result.user());
    assertEquals(teamDto, result.team());
    assertEquals(roleDto, result.role());
  }

  @Test
  void shouldMapTeamUserDtoToEntity() {
    when(userMapper.toEntity(userDto)).thenReturn(user);
    when(teamMapper.toEntity(teamDto)).thenReturn(team);
    when(roleMapper.toEntity(roleDto)).thenReturn(role);

    TeamUser result = teamUserMapper.toEntity(teamUserDto);

    assertNotNull(result);
    assertEquals(user, result.getUser());
    assertEquals(team, result.getTeam());
    assertEquals(role, result.getRole());
  }

  @Test
  void shouldHandleNullTeamUser() {
    TeamUserDto result = teamUserMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullTeamUserDto() {
    TeamUser result = teamUserMapper.toEntity(null);
    assertNull(result);
  }
}