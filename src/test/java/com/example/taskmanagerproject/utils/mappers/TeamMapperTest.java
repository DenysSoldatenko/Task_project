package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TeamMapperTest {

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private TeamMapperImpl teamMapper;

  private Team team;
  private TeamDto teamDto;

  private User creator;
  private UserDto creatorDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    creator = new User();
    creator.setId(1L);
    creator.setUsername("creator@gmail.com");

    creatorDto = new UserDto(1L, "creator@gmail.com", "Creator", "creator-slug", "", null);

    team = new Team();
    team.setId(1L);
    team.setName("Test Team");
    team.setDescription("Description");
    team.setCreator(creator);
    team.setCreatedAt(LocalDateTime.now());

    teamDto = new TeamDto(1L, "Test Team", "Description", creatorDto);
  }

  @Test
  void shouldMapTeamToDto() {
    when(userMapper.toDto(creator)).thenReturn(creatorDto);

    TeamDto result = teamMapper.toDto(team);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Test Team", result.name());
    assertEquals("Description", result.description());
    assertEquals(creatorDto, result.creator());
  }

  @Test
  void shouldMapTeamDtoToEntity() {
    when(userMapper.toEntity(creatorDto)).thenReturn(creator);

    Team result = teamMapper.toEntity(teamDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Team", result.getName());
    assertEquals("Description", result.getDescription());
    assertEquals(creator, result.getCreator());
  }

  @Test
  void shouldHandleNullTeam() {
    TeamDto result = teamMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullTeamDto() {
    Team result = teamMapper.toEntity(null);
    assertNull(result);
  }
}