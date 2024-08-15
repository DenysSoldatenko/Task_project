package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.security.User;
import com.example.taskmanagerproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service class for loading user-specific data during authentication.
 */
@Service
@RequiredArgsConstructor
public final class JwtUserDetailsService implements UserDetailsService {

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = userService.getUserByUsername(username);
    return JwtEntityFactory.create(user);
  }
}
