package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.users.User;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Factory class for creating JwtEntity objects.
 */
public final class JwtEntityFactory {

  /**
   * Creates a JwtEntity object from a User object.
   *
   * @param user The user from which to create the JwtEntity.
   * @return The JwtEntity created from the user.
   */
  public static Jwt2AuthenticationToken create(User user) {
    return new Jwt2AuthenticationToken(
      user.getId(),
      user.getFullName(),
      user.getUsername(),
      "user.getPassword()",
      mapToGrantedAuthorities("user.getRole().getName()")
    );
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(String role) {
    return Collections.singletonList(new SimpleGrantedAuthority(role));
  }
}
