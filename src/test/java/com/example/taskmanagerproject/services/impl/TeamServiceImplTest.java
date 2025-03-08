package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.TEAM_NOT_FOUND_WITH_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.teams.TeamUserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.exceptions.ResourceNotFoundException;
import com.example.taskmanagerproject.repositories.TeamRepository;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import com.example.taskmanagerproject.utils.factories.TeamFactory;
import com.example.taskmanagerproject.utils.factories.TeamUserFactory;
import com.example.taskmanagerproject.utils.mappers.TeamMapper;
import com.example.taskmanagerproject.utils.mappers.TeamUserMapper;
import com.example.taskmanagerproject.utils.validators.TeamValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

class TeamServiceImplTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamUserRepository teamUserRepository;

  @Mock
  private TeamMapper teamMapper;

  @Mock
  private TeamUserMapper teamUserMapper;

  @Mock
  private TeamFactory teamFactory;

  @Mock
  private TeamUserFactory teamUserFactory;

  @Mock
  private TeamValidator teamValidator;

  @InjectMocks
  private TeamServiceImpl teamService;

  private Team team;
  private TeamDto teamDto;
  private TeamUser teamUser;
  private TeamUserDto teamUserDto;
  private final String teamName = "TestTeam";
  private final String description = "Test Description";
  private final String slug = "test-slug";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    team = mock(Team.class);
    teamDto = mock(TeamDto.class);
    teamUser = mock(TeamUser.class);
    teamUserDto = mock(TeamUserDto.class);
    when(teamMapper.toDto(team)).thenReturn(teamDto);
    when(teamUserMapper.toDto(teamUser)).thenReturn(teamUserDto);
    when(teamDto.name()).thenReturn(teamName);
    when(teamDto.description()).thenReturn(description);
    when(team.getName()).thenReturn(teamName);
  }

  @Test
  void createTeam_shouldCreateAndReturnTeamDto() {
    doNothing().when(teamValidator).validateTeamDto(teamDto);
    when(teamFactory.createProjectFromRequest(teamDto)).thenReturn(team);
    when(teamRepository.save(team)).thenReturn(team);
    TeamDto result = teamService.createTeam(teamDto);
    assertNotNull(result);
    assertEquals(teamDto, result);
    verify(teamValidator).validateTeamDto(teamDto);
    verify(teamFactory).createProjectFromRequest(teamDto);
    verify(teamRepository).save(team);
    verify(teamMapper).toDto(team);
    verifyNoInteractions(teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void createTeam_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    doThrow(new IllegalArgumentException("Invalid team name")).when(teamValidator).validateTeamDto(teamDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> teamService.createTeam(teamDto));
    assertEquals("Invalid team name", exception.getMessage());
    verify(teamValidator).validateTeamDto(teamDto);
    verifyNoInteractions(teamFactory, teamRepository, teamMapper, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void createTeam_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(teamValidator).validateTeamDto(teamDto);
    when(teamFactory.createProjectFromRequest(teamDto)).thenReturn(team);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(teamRepository).save(team);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> teamService.createTeam(teamDto));
    assertEquals("Constraint violation", exception.getMessage());
    verify(teamValidator).validateTeamDto(teamDto);
    verify(teamFactory).createProjectFromRequest(teamDto);
    verify(teamRepository).save(team);
    verifyNoInteractions(teamMapper, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamByName_shouldReturnTeamDtoWhenTeamExists() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    TeamDto result = teamService.getTeamByName(teamName);
    assertNotNull(result);
    assertEquals(teamDto, result);
    verify(teamRepository).findByName(teamName);
    verify(teamMapper).toDto(team);
    verifyNoMoreInteractions(teamRepository, teamMapper);
    verifyNoInteractions(teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamByName_shouldThrowResourceNotFoundExceptionWhenTeamNotFound() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamByName(teamName));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + teamName, exception.getMessage());
    verify(teamRepository).findByName(teamName);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamByName_shouldHandleEmptyTeamName() {
    String emptyTeamName = "";
    when(teamRepository.findByName(emptyTeamName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamByName(emptyTeamName));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + emptyTeamName, exception.getMessage());
    verify(teamRepository).findByName(emptyTeamName);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamsBySlug_shouldReturnTeamDtoListWhenTeamsExist() {
    List<Team> teams = List.of(team);
    when(teamRepository.findByUserSlug(slug)).thenReturn(teams);
    List<TeamDto> result = teamService.getTeamsBySlug(slug);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(teamDto, result.get(0));
    verify(teamRepository).findByUserSlug(slug);
    verify(teamMapper).toDto(team);
    verifyNoInteractions(teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamsBySlug_shouldReturnEmptyListWhenNoTeams() {
    when(teamRepository.findByUserSlug(slug)).thenReturn(Collections.emptyList());
    List<TeamDto> result = teamService.getTeamsBySlug(slug);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamRepository).findByUserSlug(slug);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamsBySlug_shouldHandleEmptySlug() {
    String emptySlug = "";
    when(teamRepository.findByUserSlug(emptySlug)).thenReturn(Collections.emptyList());
    List<TeamDto> result = teamService.getTeamsBySlug(emptySlug);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamRepository).findByUserSlug(emptySlug);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getTeamsBySlug_shouldHandleMultipleTeams() {
    Team team2 = mock(Team.class);
    TeamDto teamDto2 = mock(TeamDto.class);
    List<Team> teams = List.of(team, team2);
    when(teamRepository.findByUserSlug(slug)).thenReturn(teams);
    when(teamMapper.toDto(team2)).thenReturn(teamDto2);
    List<TeamDto> result = teamService.getTeamsBySlug(slug);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(teamDto, result.get(0));
    assertEquals(teamDto2, result.get(1));
    verify(teamRepository).findByUserSlug(slug);
    verify(teamMapper).toDto(team);
    verify(teamMapper).toDto(team2);
    verifyNoInteractions(teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void getUsersWithRolesForTeam_shouldReturnTeamUserDtoListWhenUsersExist() {
    List<TeamUser> teamUsers = List.of(teamUser);
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(teamUsers);
    List<TeamUserDto> result = teamService.getUsersWithRolesForTeam(teamName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(teamUserDto, result.get(0));
    verify(teamUserRepository).findAllByTeamName(teamName);
    verify(teamUserMapper).toDto(teamUser);
    verifyNoInteractions(teamRepository, teamMapper, teamValidator, teamFactory, teamUserFactory);
  }

  @Test
  void getUsersWithRolesForTeam_shouldReturnEmptyListWhenNoUsers() {
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(Collections.emptyList());
    List<TeamUserDto> result = teamService.getUsersWithRolesForTeam(teamName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findAllByTeamName(teamName);
    verifyNoInteractions(teamUserMapper, teamRepository, teamMapper, teamValidator, teamFactory, teamUserFactory);
  }

  @Test
  void getUsersWithRolesForTeam_shouldHandleEmptyTeamName() {
    String emptyTeamName = "";
    when(teamUserRepository.findAllByTeamName(emptyTeamName)).thenReturn(Collections.emptyList());
    List<TeamUserDto> result = teamService.getUsersWithRolesForTeam(emptyTeamName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findAllByTeamName(emptyTeamName);
    verifyNoInteractions(teamUserMapper, teamRepository, teamMapper, teamValidator, teamFactory, teamUserFactory);
  }

  @Test
  void getUsersWithRolesForTeam_shouldHandleMultipleUsers() {
    TeamUser teamUser2 = mock(TeamUser.class);
    TeamUserDto teamUserDto2 = mock(TeamUserDto.class);
    List<TeamUser> teamUsers = List.of(teamUser, teamUser2);
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(teamUsers);
    when(teamUserMapper.toDto(teamUser2)).thenReturn(teamUserDto2);
    List<TeamUserDto> result = teamService.getUsersWithRolesForTeam(teamName);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(teamUserDto, result.get(0));
    assertEquals(teamUserDto2, result.get(1));
    verify(teamUserRepository).findAllByTeamName(teamName);
    verify(teamUserMapper).toDto(teamUser);
    verify(teamUserMapper).toDto(teamUser2);
    verifyNoInteractions(teamRepository, teamMapper, teamValidator, teamFactory, teamUserFactory);
  }

  @Test
  void updateTeam_shouldUpdateAndReturnTeamDto() {
    doNothing().when(teamValidator).validateTeamDto(teamDto, team);
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    when(teamRepository.save(team)).thenReturn(team);
    TeamDto result = teamService.updateTeam(teamName, teamDto);
    assertNotNull(result);
    assertEquals(teamDto, result);
    verify(teamRepository).findByName(teamName);
    verify(teamValidator).validateTeamDto(teamDto, team);
    verify(team).setName(teamName);
    verify(team).setDescription(description);
    verify(teamRepository).save(team);
    verify(teamMapper).toDto(team);
    verifyNoInteractions(teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void updateTeam_shouldThrowResourceNotFoundExceptionWhenTeamNotFound() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> teamService.updateTeam(teamName, teamDto));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + teamName, exception.getMessage());
    verify(teamRepository).findByName(teamName);
    verifyNoInteractions(teamValidator, teamMapper, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void updateTeam_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    doThrow(new IllegalArgumentException("Invalid team name")).when(teamValidator).validateTeamDto(teamDto, team);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> teamService.updateTeam(teamName, teamDto));
    assertEquals("Invalid team name", exception.getMessage());
    verify(teamRepository).findByName(teamName);
    verify(teamValidator).validateTeamDto(teamDto, team);
    verifyNoInteractions(teamMapper, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void updateTeam_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(teamValidator).validateTeamDto(teamDto, team);
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(teamRepository).save(team);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> teamService.updateTeam(teamName, teamDto));
    assertEquals("Constraint violation", exception.getMessage());
    verify(teamRepository).findByName(teamName);
    verify(teamValidator).validateTeamDto(teamDto, team);
    verify(team).setName(teamName);
    verify(team).setDescription(description);
    verify(teamRepository).save(team);
    verifyNoInteractions(teamMapper, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void deleteTeam_shouldDeleteTeamWhenExists() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
    doNothing().when(teamRepository).delete(team);
    teamService.deleteTeam(teamName);
    verify(teamRepository).findByName(teamName);
    verify(teamRepository).delete(team);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void deleteTeam_shouldThrowResourceNotFoundExceptionWhenTeamNotFound() {
    when(teamRepository.findByName(teamName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> teamService.deleteTeam(teamName));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + teamName, exception.getMessage());
    verify(teamRepository).findByName(teamName);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void deleteTeam_shouldHandleEmptyTeamName() {
    String emptyTeamName = "";
    when(teamRepository.findByName(emptyTeamName)).thenReturn(Optional.empty());
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> teamService.deleteTeam(emptyTeamName));
    assertEquals(TEAM_NOT_FOUND_WITH_NAME + emptyTeamName, exception.getMessage());
    verify(teamRepository).findByName(emptyTeamName);
    verifyNoInteractions(teamMapper, teamValidator, teamFactory, teamUserRepository, teamUserMapper, teamUserFactory);
  }

  @Test
  void addUsersToTeam_shouldAddUsersAndReturnTeamUserDtoList() {
    List<TeamUserDto> teamUserDtoList = List.of(teamUserDto);
    List<TeamUser> teamUsers = List.of(teamUser);
    when(teamUserFactory.createUserTeamAssociations(teamUserDtoList)).thenReturn(teamUsers);
    when(teamUserRepository.saveAll(teamUsers)).thenReturn(teamUsers);
    List<TeamUserDto> result = teamService.addUsersToTeam(teamName, teamUserDtoList);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(teamUserDto, result.get(0));
    verify(teamUserFactory).createUserTeamAssociations(teamUserDtoList);
    verify(teamUserRepository).saveAll(teamUsers);
    verify(teamUserMapper).toDto(teamUser);
    verifyNoInteractions(teamRepository, teamMapper, teamValidator, teamFactory);
  }

  @Test
  void addUsersToTeam_shouldReturnEmptyListWhenNoUsersAdded() {
    List<TeamUserDto> teamUserDtoList = Collections.emptyList();
    List<TeamUser> teamUsers = Collections.emptyList();
    when(teamUserFactory.createUserTeamAssociations(teamUserDtoList)).thenReturn(teamUsers);
    when(teamUserRepository.saveAll(teamUsers)).thenReturn(teamUsers);
    List<TeamUserDto> result = teamService.addUsersToTeam(teamName, teamUserDtoList);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamUserFactory).createUserTeamAssociations(teamUserDtoList);
    verify(teamUserRepository).saveAll(teamUsers);
    verifyNoInteractions(teamUserMapper, teamRepository, teamMapper, teamValidator, teamFactory);
  }

  @Test
  void addUsersToTeam_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    List<TeamUserDto> teamUserDtoList = List.of(teamUserDto);
    List<TeamUser> teamUsers = List.of(teamUser);
    when(teamUserFactory.createUserTeamAssociations(teamUserDtoList)).thenReturn(teamUsers);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(teamUserRepository).saveAll(teamUsers);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> teamService.addUsersToTeam(teamName, teamUserDtoList));
    assertEquals("Constraint violation", exception.getMessage());
    verify(teamUserFactory).createUserTeamAssociations(teamUserDtoList);
    verify(teamUserRepository).saveAll(teamUsers);
    verifyNoInteractions(teamUserMapper, teamRepository, teamMapper, teamValidator, teamFactory);
  }

  @Test
  void addUsersToTeam_shouldHandleMultipleUsers() {
    TeamUser teamUser2 = mock(TeamUser.class);
    TeamUserDto teamUserDto2 = mock(TeamUserDto.class);
    List<TeamUserDto> teamUserDtoList = List.of(teamUserDto, teamUserDto2);
    List<TeamUser> teamUsers = List.of(teamUser, teamUser2);
    when(teamUserFactory.createUserTeamAssociations(teamUserDtoList)).thenReturn(teamUsers);
    when(teamUserRepository.saveAll(teamUsers)).thenReturn(teamUsers);
    when(teamUserMapper.toDto(teamUser2)).thenReturn(teamUserDto2);
    List<TeamUserDto> result = teamService.addUsersToTeam(teamName, teamUserDtoList);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(teamUserDto, result.get(0));
    assertEquals(teamUserDto2, result.get(1));
    verify(teamUserFactory).createUserTeamAssociations(teamUserDtoList);
    verify(teamUserRepository).saveAll(teamUsers);
    verify(teamUserMapper).toDto(teamUser);
    verify(teamUserMapper).toDto(teamUser2);
    verifyNoInteractions(teamRepository, teamMapper, teamValidator, teamFactory);
  }
}