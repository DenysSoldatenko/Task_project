package com.example.taskmanagerproject.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.exceptions.UserNotFoundException;
import com.example.taskmanagerproject.mappers.UserMapper;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.utils.UserFactory;
import com.example.taskmanagerproject.utils.UserValidator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("User Service Tests")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserFactory userFactory;

  @Mock
  private UserValidator userValidator;

  @InjectMocks
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("Get User By Id - Success")
  void getUserById_Success() {
    Long userId = 1L;
    User user = new User();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123"));

    UserDto result = userService.getUserById(userId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Get User By Id - User Not Found")
  void getUserById_UserNotFound() {
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
  }

  @Test
  @DisplayName("Get User By Username - Success")
  void getUserByUsername_Success() {
    String username = "testuser";
    User user = new User();
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

    User result = userService.getUserByUsername(username);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Get User By Username - User Not Found")
  void getUserByUsername_UserNotFound() {
    String username = "testuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername(username));
  }

  @Test
  @DisplayName("Update User - Success")
  void updateUser_Success() {
    Long userId = 1L;
    UserDto userDto = new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123");
    User user = new User();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDto(user)).thenReturn(userDto);

    UserDto result = userService.updateUser(userDto, userId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Create User - Success")
  void createUser_Success() {
    UserDto userDto = new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123");
    User user = new User();
    when(userFactory.createUserFromRequest(userDto)).thenReturn(user);

    User result = userService.createUser(userDto);

    assertNotNull(result);
    verify(userValidator, times(1)).validateUserDto(userDto);
    verify(userRepository, times(1)).save(user);
  }

  @Test
  @DisplayName("Is User Task Owner - Success")
  void isUserTaskOwner_Success() {
    Long userId = 1L;
    Long taskId = 1L;
    when(userRepository.isTaskOwner(userId, taskId)).thenReturn(true);

    boolean result = userService.isUserTaskOwner(userId, taskId);

    assertTrue(result);
  }

  @Test
  @DisplayName("Get Task Author - Success")
  void getTaskAuthor_Success() {
    Long taskId = 1L;
    User user = new User();
    when(userRepository.findTaskAuthorByTaskId(taskId)).thenReturn(Optional.of(user));
    when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123"));

    UserDto result = userService.getTaskAuthor(taskId);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Delete User By Id - Success")
  void deleteUserById_Success() {
    Long userId = 1L;
    User user = new User();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertDoesNotThrow(() -> userService.deleteUserById(userId));

    verify(userRepository, times(1)).delete(user);
  }
}
