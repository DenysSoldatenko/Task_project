package com.example.taskmanagerproject.services;

import com.example.taskmanagerproject.dtos.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.UserDto;

/**
 * Service class for handling user authentication and registration.
 */
public interface AuthenticationService {

  AuthenticationResponse registerUser(UserDto request);

  AuthenticationResponse authenticate(AuthenticationRequest request);
}