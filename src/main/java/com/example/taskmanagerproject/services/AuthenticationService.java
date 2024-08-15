package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.security.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.security.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.security.UserDto;

/**
 * Service class for handling user authentication and registration.
 */
public interface AuthenticationService {

  /**
   * Registers a new user with the provided user data.
   *
   * @param request The UserDto containing the user's registration details.
   * @return The AuthenticationResponse containing the user's authentication token and details.
   */
  AuthenticationResponse registerUser(UserDto request);

  /**
   * Authenticates a user with the provided credentials.
   *
   * @param request The AuthenticationRequest containing the user's credentials.
   * @return The AuthenticationResponse containing the authentication token.
   */
  AuthenticationResponse authenticate(AuthenticationRequest request);
}
