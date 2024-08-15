package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.entities.security.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.security.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.security.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.security.UserDto;
import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.security.JwtTokenProvider;
import com.example.taskmanagerproject.services.UserService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@DisplayName("Authentication Service Tests")
class AuthenticationServiceImplTest {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImplTest.class);

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private UserService userService;

  @InjectMocks
  private AuthenticationServiceImpl authenticationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("Register User - Success")
  void registerUser_Success() {
    UserDto userDto = new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123");
    User user = new User();
    when(userService.createUser(userDto)).thenReturn(user);
    when(jwtTokenProvider.createAccessToken(any(), any(), any())).thenReturn("mocked_jwt_token");

    AuthenticationResponse response = authenticationService.registerUser(userDto);
    assertEquals("mocked_jwt_token", response.token());

    logger.info("Registration successful for user: {}", userDto.username());
  }

  @Test
  @DisplayName("Authenticate User - Success")
  void authenticate_Success() {
    AuthenticationRequest request = new AuthenticationRequest("username", "password");
    User user = new User();
    when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
    when(jwtTokenProvider.createAccessToken(any(), any(), any())).thenReturn("mocked_jwt_token");

    AuthenticationResponse response = authenticationService.authenticate(request);
    assertEquals("mocked_jwt_token", response.token());

    logger.info("Authentication successful for username: {}", request.username());
  }

  @Test
  @DisplayName("Authenticate User - Username Not Found")
  void authenticate_UsernameNotFound() {
    AuthenticationRequest request = new AuthenticationRequest("username", "password");
    when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(request));

    logger.error("Username not found: {}", request.username());
  }

  @Test
  @DisplayName("Register User - Fail when UserService throws Exception")
  void registerUser_Fail_WhenUserServiceThrowsException() {
    UserDto userDto = new UserDto(1L, "Alice Johnson", "alice123@gmail.com", "password123", "password123");
    when(userService.createUser(userDto)).thenThrow(new RuntimeException("User service exception"));

    assertThrows(RuntimeException.class, () -> authenticationService.registerUser(userDto));
    verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());

    logger.error("User registration failed for user: {}", userDto.username());
  }

  @Test
  @DisplayName("Authenticate User - Fail when Invalid Credentials")
  void authenticate_Fail_WhenInvalidCredentials() {
    AuthenticationRequest request = new AuthenticationRequest("username", "password");
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

    assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());

    logger.error("Authentication failed for username: {}", request.username());
  }

  @Test
  @DisplayName("Authenticate User - Success when User Has Roles")
  void authenticate_Success_WhenUserHasRoles() {
    AuthenticationRequest request = new AuthenticationRequest("username", "password");
    User user = new User();
    user.setUserRoles(Collections.singleton(ROLE_USER));
    when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
    when(jwtTokenProvider.createAccessToken(any(), any(), any())).thenReturn("mocked_jwt_token");

    AuthenticationResponse response = authenticationService.authenticate(request);
    assertEquals("mocked_jwt_token", response.token());

    logger.info("Authentication successful for username: {}", request.username());
  }
}
