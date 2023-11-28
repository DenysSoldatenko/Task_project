package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.User;
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
public class JwtUserDetailsService implements UserDetailsService {

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(final String username) {
    User user = userService.getUserByUsername(username);
    return JwtEntityFactory.create(user);
  }
}
