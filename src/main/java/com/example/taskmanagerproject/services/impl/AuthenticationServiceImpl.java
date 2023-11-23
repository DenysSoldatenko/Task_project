package com.example.taskmanagerproject.services.impl;

import com.example.taskmanagerproject.dtos.AuthenticationRequest;
import com.example.taskmanagerproject.dtos.AuthenticationResponse;
import com.example.taskmanagerproject.dtos.UserDto;
import com.example.taskmanagerproject.services.AuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  @Override
  public AuthenticationResponse registerUser(UserDto request) {
    return null;
  }

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    return null;
  }

  @Override
  public void authenticateUser(AuthenticationRequest request) {

  }

  @Override
  public AuthenticationResponse createAuthenticationResponse(UserDto user) {
    return null;
  }
}
