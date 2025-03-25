package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_SLUG;
import static com.example.taskmanagerproject.utils.MessageUtil.USER_NOT_FOUND_WITH_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.dtos.users.UserImageDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.services.ImageService;
import com.example.taskmanagerproject.utils.factories.UserFactory;
import com.example.taskmanagerproject.utils.mappers.UserMapper;
import com.example.taskmanagerproject.utils.validators.UserValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private UserFactory userFactory;

  @Mock
  private UserImageDto imageDto;

  @Mock
  private ImageService imageService;

  @Mock
  private UserValidator userValidator;

  @InjectMocks
  private UserServiceImpl userService;

  private User user;
  private UserDto userDto;
  private JwtAuthenticationToken jwtAuth;

  private final Long userId = 1L;
  private final Long taskId = 1L;

  private final String slug = "test-slug";
  private final String fullName = "Test User";
  private final String username = "testuser";
  private final String teamName = "TestTeam";
  private final String projectName = "TestProject";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    user = mock(User.class);
    userDto = mock(UserDto.class);
    imageDto = mock(UserImageDto.class);
    jwtAuth = mock(JwtAuthenticationToken.class);

    when(userMapper.toDto(user)).thenReturn(userDto);
    when(userDto.fullName()).thenReturn(fullName);
    when(userDto.slug()).thenReturn(slug);
    when(user.getImage()).thenReturn(new ArrayList<>());
  }

  @Test
  void getUserBySlug_shouldReturnUserDtoWhenUserExists() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    UserDto result = userService.getUserBySlug(slug);
    assertNotNull(result);
    assertEquals(userDto, result);
    verify(userRepository).findBySlug(slug);
    verify(userMapper).toDto(user);
    verifyNoMoreInteractions(userRepository, userMapper);
    verifyNoInteractions(userFactory, imageService, userValidator);
  }

  @Test
  void getUserBySlug_shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserBySlug(slug));
    assertEquals(USER_NOT_FOUND_WITH_SLUG + slug, exception.getMessage());
    verify(userRepository).findBySlug(slug);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void getUserBySlug_shouldHandleEmptySlug() {
    String emptySlug = "";
    when(userRepository.findBySlug(emptySlug)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserBySlug(emptySlug));
    assertEquals(USER_NOT_FOUND_WITH_SLUG + emptySlug, exception.getMessage());
    verify(userRepository).findBySlug(emptySlug);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void getUserByUsername_shouldReturnUserWhenUserExists() {
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    User result = userService.getUserByUsername(username);
    assertNotNull(result);
    assertEquals(user, result);
    verify(userRepository).findByUsername(username);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void getUserByUsername_shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserByUsername(username));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + username, exception.getMessage());
    verify(userRepository).findByUsername(username);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void getUserByUsername_shouldHandleEmptyUsername() {
    String emptyUsername = "";
    when(userRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.getUserByUsername(emptyUsername));
    assertEquals(USER_NOT_FOUND_WITH_USERNAME + emptyUsername, exception.getMessage());
    verify(userRepository).findByUsername(emptyUsername);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void updateUser_shouldUpdateAndReturnUserDto() {
    doNothing().when(userValidator).validateUserDto(userDto);
    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);
    UserDto result = userService.updateUser(userDto, slug);
    assertNotNull(result);
    assertEquals(userDto, result);
    verify(userRepository).findBySlug(slug);
    verify(userValidator).validateUserDto(userDto);
    verify(user).setFullName(fullName);
    verify(user).setSlug(slug);
    verify(userRepository).save(user);
    verify(userMapper).toDto(user);
    verifyNoInteractions(userFactory, imageService);
  }

  @Test
  void updateUser_shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.updateUser(userDto, slug));
    assertEquals(USER_NOT_FOUND_WITH_SLUG + slug, exception.getMessage());
    verify(userRepository).findBySlug(slug);
    verifyNoInteractions(userValidator, userMapper, userFactory, imageService);
  }

  @Test
  void updateUser_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    doThrow(new IllegalArgumentException("Invalid user data")).when(userValidator).validateUserDto(userDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userDto, slug));
    assertEquals("Invalid user data", exception.getMessage());
    verify(userRepository).findBySlug(slug);
    verify(userValidator).validateUserDto(userDto);
    verifyNoInteractions(userMapper, userFactory, imageService);
  }

  @Test
  void updateUser_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    doNothing().when(userValidator).validateUserDto(userDto);
    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(userRepository).save(user);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> userService.updateUser(userDto, slug));
    assertEquals("Constraint violation", exception.getMessage());
    verify(userRepository).findBySlug(slug);
    verify(userValidator).validateUserDto(userDto);
    verify(user).setFullName(fullName);
    verify(user).setSlug(slug);
    verify(userRepository).save(user);
    verifyNoInteractions(userMapper, userFactory, imageService);
  }

  @Test
  void createUser_shouldCreateAndReturnUserDto() {
    when(userFactory.createUserFromRequest(jwtAuth)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userDto);
    doNothing().when(userValidator).validateUserDto(userDto);

    UserDto result = userService.createUser(jwtAuth);

    assertNotNull(result);
    assertEquals(userDto, result);

    verify(userFactory).createUserFromRequest(jwtAuth);
    verify(userRepository).save(user);
    verify(userValidator).validateUserDto(userDto);
    verify(userMapper, atLeastOnce()).toDto(user);
    verifyNoInteractions(imageService);
  }


  @Test
  void createUser_shouldThrowIllegalArgumentExceptionWhenDtoInvalid() {
    when(userFactory.createUserFromRequest(jwtAuth)).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userDto);
    doThrow(new IllegalArgumentException("Invalid user data")).when(userValidator).validateUserDto(userDto);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(jwtAuth));
    assertEquals("Invalid user data", exception.getMessage());
    verify(userFactory).createUserFromRequest(jwtAuth);
    verify(userMapper).toDto(user);
    verify(userValidator).validateUserDto(userDto);
    verifyNoInteractions(userRepository, imageService);
  }

  @Test
  void createUser_shouldThrowDataIntegrityViolationExceptionWhenSaveFails() {
    when(userFactory.createUserFromRequest(jwtAuth)).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userDto);
    doNothing().when(userValidator).validateUserDto(userDto);
    doThrow(new DataIntegrityViolationException("Constraint violation")).when(userRepository).save(user);
    DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(jwtAuth));
    assertEquals("Constraint violation", exception.getMessage());
    verify(userFactory).createUserFromRequest(jwtAuth);
    verify(userMapper).toDto(user);
    verify(userValidator).validateUserDto(userDto);
    verify(userRepository).save(user);
    verifyNoInteractions(imageService);
  }

  @Test
  void isUserTaskOwner_shouldReturnTrueWhenUserIsOwner() {
    when(userRepository.isTaskOwner(userId, taskId)).thenReturn(true);
    boolean result = userService.isUserTaskOwner(userId, taskId);
    assertTrue(result);
    verify(userRepository).isTaskOwner(userId, taskId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void isUserTaskOwner_shouldReturnFalseWhenUserIsNotOwner() {
    when(userRepository.isTaskOwner(userId, taskId)).thenReturn(false);
    boolean result = userService.isUserTaskOwner(userId, taskId);
    assertFalse(result);
    verify(userRepository).isTaskOwner(userId, taskId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void isUserTaskOwner_shouldHandleZeroIds() {
    when(userRepository.isTaskOwner(0L, 0L)).thenReturn(false);
    boolean result = userService.isUserTaskOwner(0L, 0L);
    assertFalse(result);
    verify(userRepository).isTaskOwner(0L, 0L);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void deleteUserBySlug_shouldDeleteUserWhenExists() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    doNothing().when(userRepository).delete(user);
    userService.deleteUserBySlug(slug);
    verify(userRepository).findBySlug(slug);
    verify(userRepository).delete(user);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void deleteUserBySlug_shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
    when(userRepository.findBySlug(slug)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.deleteUserBySlug(slug));
    assertEquals(USER_NOT_FOUND_WITH_SLUG + slug, exception.getMessage());
    verify(userRepository).findBySlug(slug);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void deleteUserBySlug_shouldHandleEmptySlug() {
    String emptySlug = "";
    when(userRepository.findBySlug(emptySlug)).thenReturn(Optional.empty());
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userService.deleteUserBySlug(emptySlug));
    assertEquals(USER_NOT_FOUND_WITH_SLUG + emptySlug, exception.getMessage());
    verify(userRepository).findBySlug(emptySlug);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void hasTeamAccess_shouldReturnTrueWhenUserIsCreator() {
    when(userRepository.isTeamCreator(teamName, username)).thenReturn(true);
    boolean result = userService.hasTeamAccess(teamName, username);
    assertTrue(result);
    verify(userRepository).isTeamCreator(teamName, username);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void hasTeamAccess_shouldReturnTrueWhenUserInLeadershipPosition() {
    when(userRepository.isTeamCreator(teamName, username)).thenReturn(false);
    when(userRepository.isUserInLeadershipPositionInTeam(teamName, username)).thenReturn(true);
    boolean result = userService.hasTeamAccess(teamName, username);
    assertTrue(result);
    verify(userRepository).isTeamCreator(teamName, username);
    verify(userRepository).isUserInLeadershipPositionInTeam(teamName, username);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void hasTeamAccess_shouldReturnFalseWhenNoAccess() {
    when(userRepository.isTeamCreator(teamName, username)).thenReturn(false);
    when(userRepository.isUserInLeadershipPositionInTeam(teamName, username)).thenReturn(false);
    boolean result = userService.hasTeamAccess(teamName, username);
    assertFalse(result);
    verify(userRepository).isTeamCreator(teamName, username);
    verify(userRepository).isUserInLeadershipPositionInTeam(teamName, username);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void hasProjectAccess_shouldReturnTrueWhenUserIsCreator() {
    when(userRepository.isProjectCreator(projectName, username)).thenReturn(true);
    boolean result = userService.hasProjectAccess(projectName, username);
    assertTrue(result);
    verify(userRepository).isProjectCreator(projectName, username);
  }

  @Test
  void hasProjectAccess_shouldReturnTrueWhenUserInLeadershipPosition() {
    when(userRepository.isProjectCreator(projectName, username)).thenReturn(false);
    when(userRepository.isUserInLeadershipPositionInProject(projectName, username)).thenReturn(true);
    boolean result = userService.hasProjectAccess(projectName, username);
    assertTrue(result);
    verify(userRepository).isProjectCreator(projectName, username);
    verify(userRepository).isUserInLeadershipPositionInProject(projectName, username);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void hasProjectAccess_shouldReturnFalseWhenNoAccess() {
    when(userRepository.isProjectCreator(projectName, username)).thenReturn(false);
    when(userRepository.isUserInLeadershipPositionInProject(projectName, username)).thenReturn(false);
    boolean result = userService.hasProjectAccess(projectName, username);
    assertFalse(result);
    verify(userRepository).isProjectCreator(projectName, username);
    verify(userRepository).isUserInLeadershipPositionInProject(projectName, username);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void isUserAssignedToTask_shouldReturnTrueWhenAssigned() {
    when(userRepository.isUserAssignedToTask(userId, taskId)).thenReturn(true);
    boolean result = userService.isUserAssignedToTask(userId, taskId);
    assertTrue(result);
    verify(userRepository).isUserAssignedToTask(userId, taskId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void isUserAssignedToTask_shouldReturnFalseWhenNotAssigned() {
    when(userRepository.isUserAssignedToTask(userId, taskId)).thenReturn(false);
    boolean result = userService.isUserAssignedToTask(userId, taskId);
    assertFalse(result);
    verify(userRepository).isUserAssignedToTask(userId, taskId);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void isUserAssignedToTask_shouldHandleZeroIds() {
    when(userRepository.isUserAssignedToTask(0L, 0L)).thenReturn(false);
    boolean result = userService.isUserAssignedToTask(0L, 0L);
    assertFalse(result);
    verify(userRepository).isUserAssignedToTask(0L, 0L);
    verifyNoMoreInteractions(userRepository);
    verifyNoInteractions(userMapper, userFactory, imageService, userValidator);
  }

  @Test
  void uploadUserPhoto_shouldUploadAndSaveImage() {
    String slug = "test-slug";
    String fileName = "uploaded.jpg";
    User user = new User();
    user.setImage(new ArrayList<>());

    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    when(imageService.uploadUserImage(imageDto)).thenReturn(fileName);

    userService.uploadUserPhoto(slug, imageDto);

    assertTrue(user.getImage().contains(fileName));
    verify(userRepository).findBySlug(slug);
    verify(imageService).uploadUserImage(imageDto);
    verify(userRepository).save(user);
  }

  @Test
  void updateUserPhoto_shouldReplaceOldImage() {
    String slug = "test-slug";
    String newFileName = "new-photo.jpg";
    User user = new User();
    user.setImage(new ArrayList<>(List.of("old-photo.jpg")));

    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));
    when(imageService.uploadUserImage(imageDto)).thenReturn(newFileName);

    userService.updateUserPhoto(slug, imageDto);

    assertEquals(1, user.getImage().size());
    assertTrue(user.getImage().contains(newFileName));
    verify(userRepository, times(2)).findBySlug(slug); // once in update, once in delete
    verify(imageService).uploadUserImage(imageDto);
    verify(userRepository, times(2)).save(user); // once in delete, once in update
  }

  @Test
  void deleteUserPhoto_shouldClearImages() {
    String slug = "test-slug";
    User user = new User();
    user.setImage(new ArrayList<>(List.of("some-photo.jpg")));

    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));

    userService.deleteUserPhoto(slug);

    assertTrue(user.getImage().isEmpty());
    verify(userRepository).findBySlug(slug);
    verify(userRepository).save(user);
  }

  @Test
  void deleteUserPhoto_shouldDoNothingIfNoImages() {
    String slug = "test-slug";
    User user = new User();
    user.setImage(null);

    when(userRepository.findBySlug(slug)).thenReturn(Optional.of(user));

    userService.deleteUserPhoto(slug);

    verify(userRepository).findBySlug(slug);
    verify(userRepository, never()).save(user);
  }
}