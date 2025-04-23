package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.entities.roles.Role;
import com.example.taskmanagerproject.entities.teams.TeamUser;
import com.example.taskmanagerproject.repositories.TeamUserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TeamUserServiceImplTest {

  @Mock
  private TeamUserRepository teamUserRepository;

  @InjectMocks
  private TeamUserServiceImpl teamUserService;

  private Role role;
  private TeamUser teamUser;

  private final Long teamId = 1L;
  private final Long userId = 1L;

  private final String teamName = "TestTeam";
  private final String username = "testuser";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    teamUser = mock(TeamUser.class);
    role = mock(Role.class);
  }

  @Test
  void getAllByTeamName_shouldReturnTeamUserListWhenUsersExist() {
    List<TeamUser> teamUsers = List.of(teamUser);
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(teamUsers);
    List<TeamUser> result = teamUserService.getAllByTeamName(teamName);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(teamUser, result.get(0));
    verify(teamUserRepository).findAllByTeamName(teamName);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getAllByTeamName_shouldReturnEmptyListWhenNoUsers() {
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(Collections.emptyList());
    List<TeamUser> result = teamUserService.getAllByTeamName(teamName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findAllByTeamName(teamName);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getAllByTeamName_shouldHandleEmptyTeamName() {
    String emptyTeamName = "";
    when(teamUserRepository.findAllByTeamName(emptyTeamName)).thenReturn(Collections.emptyList());
    List<TeamUser> result = teamUserService.getAllByTeamName(emptyTeamName);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findAllByTeamName(emptyTeamName);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getAllByTeamName_shouldHandleMultipleUsers() {
    TeamUser teamUser2 = mock(TeamUser.class);
    List<TeamUser> teamUsers = List.of(teamUser, teamUser2);
    when(teamUserRepository.findAllByTeamName(teamName)).thenReturn(teamUsers);
    List<TeamUser> result = teamUserService.getAllByTeamName(teamName);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(teamUser, result.get(0));
    assertEquals(teamUser2, result.get(1));
    verify(teamUserRepository).findAllByTeamName(teamName);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRoleByTeamNameAndUsername_shouldReturnRoleWhenFound() {
    when(teamUserRepository.findRoleByTeamNameAndUsername(teamName, username)).thenReturn(role);
    Role result = teamUserService.getRoleByTeamNameAndUsername(teamName, username);
    assertNotNull(result);
    assertEquals(role, result);
    verify(teamUserRepository).findRoleByTeamNameAndUsername(teamName, username);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRoleByTeamNameAndUsername_shouldReturnNullWhenNotFound() {
    when(teamUserRepository.findRoleByTeamNameAndUsername(teamName, username)).thenReturn(null);
    Role result = teamUserService.getRoleByTeamNameAndUsername(teamName, username);
    assertNull(result);
    verify(teamUserRepository).findRoleByTeamNameAndUsername(teamName, username);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRoleByTeamNameAndUsername_shouldHandleEmptyTeamName() {
    String emptyTeamName = "";
    when(teamUserRepository.findRoleByTeamNameAndUsername(emptyTeamName, username)).thenReturn(null);
    Role result = teamUserService.getRoleByTeamNameAndUsername(emptyTeamName, username);
    assertNull(result);
    verify(teamUserRepository).findRoleByTeamNameAndUsername(emptyTeamName, username);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRoleByTeamNameAndUsername_shouldHandleEmptyUsername() {
    String emptyUsername = "";
    when(teamUserRepository.findRoleByTeamNameAndUsername(teamName, emptyUsername)).thenReturn(null);
    Role result = teamUserService.getRoleByTeamNameAndUsername(teamName, emptyUsername);
    assertNull(result);
    verify(teamUserRepository).findRoleByTeamNameAndUsername(teamName, emptyUsername);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRandomHigherRoleUser_shouldReturnUserIdWhenFound() {
    Long higherRoleUserId = 2L;
    when(teamUserRepository.findRandomHigherRoleUser(teamId, userId)).thenReturn(Optional.of(higherRoleUserId));
    Optional<Long> result = teamUserService.getRandomHigherRoleUser(teamId, userId);
    assertTrue(result.isPresent());
    assertEquals(higherRoleUserId, result.get());
    verify(teamUserRepository).findRandomHigherRoleUser(teamId, userId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRandomHigherRoleUser_shouldReturnEmptyWhenNotFound() {
    when(teamUserRepository.findRandomHigherRoleUser(teamId, userId)).thenReturn(Optional.empty());
    Optional<Long> result = teamUserService.getRandomHigherRoleUser(teamId, userId);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findRandomHigherRoleUser(teamId, userId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRandomHigherRoleUser_shouldHandleZeroTeamId() {
    when(teamUserRepository.findRandomHigherRoleUser(0L, userId)).thenReturn(Optional.empty());
    Optional<Long> result = teamUserService.getRandomHigherRoleUser(0L, userId);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findRandomHigherRoleUser(0L, userId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void getRandomHigherRoleUser_shouldHandleZeroUserId() {
    when(teamUserRepository.findRandomHigherRoleUser(teamId, 0L)).thenReturn(Optional.empty());
    Optional<Long> result = teamUserService.getRandomHigherRoleUser(teamId, 0L);
    assertTrue(result.isEmpty());
    verify(teamUserRepository).findRandomHigherRoleUser(teamId, 0L);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void existsByUserIdAndTeamId_shouldReturnTrueWhenExists() {
    when(teamUserRepository.existsByUserIdAndTeamId(userId, teamId)).thenReturn(true);
    boolean result = teamUserService.existsByUserIdAndTeamId(userId, teamId);
    assertTrue(result);
    verify(teamUserRepository).existsByUserIdAndTeamId(userId, teamId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void existsByUserIdAndTeamId_shouldReturnFalseWhenNotExists() {
    when(teamUserRepository.existsByUserIdAndTeamId(userId, teamId)).thenReturn(false);
    boolean result = teamUserService.existsByUserIdAndTeamId(userId, teamId);
    assertFalse(result);
    verify(teamUserRepository).existsByUserIdAndTeamId(userId, teamId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void existsByUserIdAndTeamId_shouldHandleZeroUserId() {
    when(teamUserRepository.existsByUserIdAndTeamId(0L, teamId)).thenReturn(false);
    boolean result = teamUserService.existsByUserIdAndTeamId(0L, teamId);
    assertFalse(result);
    verify(teamUserRepository).existsByUserIdAndTeamId(0L, teamId);
    verifyNoMoreInteractions(teamUserRepository);
  }

  @Test
  void existsByUserIdAndTeamId_shouldHandleZeroTeamId() {
    when(teamUserRepository.existsByUserIdAndTeamId(userId, 0L)).thenReturn(false);
    boolean result = teamUserService.existsByUserIdAndTeamId(userId, 0L);
    assertFalse(result);
    verify(teamUserRepository).existsByUserIdAndTeamId(userId, 0L);
    verifyNoMoreInteractions(teamUserRepository);
  }
}