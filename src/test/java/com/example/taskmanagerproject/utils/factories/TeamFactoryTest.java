package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.teams.TeamDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.teams.Team;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class TeamFactoryTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TeamFactory teamFactory;

  private User creator;
  private TeamDto teamDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    UserDto creatorDto = new UserDto(1L, "Creator", "creator@gmail.com", "creator-slug", "", List.of(""));
    teamDto = new TeamDto(1L, "Test Team", "Description", creatorDto);

    creator = new User();
    creator.setId(1L);
    creator.setUsername("creator@gmail.com");
  }

  @Test
  void shouldCreateTeamFromRequest() {
    when(userRepository.findByUsername("creator@gmail.com")).thenReturn(Optional.of(creator));

    Team result = teamFactory.createProjectFromRequest(teamDto);

    assertEquals("Test Team", result.getName());
    assertEquals("Description", result.getDescription());
    assertEquals(creator, result.getCreator());
    assertNotNull(result.getCreatedAt());
    assertTrue(result.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
  }

  @Test
  void shouldThrowIfCreatorNotFound() {
    when(userRepository.findByUsername("creator@gmail.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> teamFactory.createProjectFromRequest(teamDto));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + "creator@gmail.com", ex.getMessage());
  }

  @Test
  void shouldCreateTeamWithEmptyDescription() {
    UserDto creatorDto = new UserDto(1L, "Creator", "creator@gmail.com", "creator-slug", "", List.of(""));
    TeamDto emptyDescDto = new TeamDto(1L, "Test Team", null, creatorDto);

    when(userRepository.findByUsername("creator@gmail.com")).thenReturn(Optional.of(creator));

    Team result = teamFactory.createProjectFromRequest(emptyDescDto);

    assertEquals("Test Team", result.getName());
    assertNull(result.getDescription());
    assertEquals(creator, result.getCreator());
    assertNotNull(result.getCreatedAt());
  }
}