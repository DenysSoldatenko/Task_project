package com.example.taskmanagerproject.services.impl;

import static com.example.taskmanagerproject.utils.MessageUtils.USER_NOT_FOUND_WITH_USERNAME;

import com.example.taskmanagerproject.dtos.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.entities.User;
import com.example.taskmanagerproject.repositories.UserRepository;
import com.example.taskmanagerproject.security.JwtTokenProvider;
import com.example.taskmanagerproject.services.AuthenticationService;
import com.example.taskmanagerproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of the AuthenticationService interface.
 */
@Service
@RequiredArgsConstructor
public final class AuthenticationServiceImpl implements AuthenticationService {

  private final UserService userService;
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;

  /**
   * Registers a new user based on the provided registration request.
   *
   * @param request The registration request containing user details.
   * @return AuthenticationResponse containing the JWT token.
   */
  public AuthenticationResponse registerUser(UserDto request) {
    User user = userService.createUser(request);
    return createAuthenticationResponse(user);
  }

  /**
   * Authenticates a user based on the provided authentication request.
   *
   * @param request The authentication request containing user credentials.
   * @return AuthenticationResponse containing the JWT token.
   */
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticateUser(request);

    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_USERNAME + request.username()));

    return createAuthenticationResponse(user);
  }

  private void authenticateUser(AuthenticationRequest request) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
  }

  private AuthenticationResponse createAuthenticationResponse(User user) {
    String jwtToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRole().getName());
    return new AuthenticationResponse(jwtToken);
  }
}
