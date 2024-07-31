package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

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
  public static JwtEntity create(final User user) {
    return new JwtEntity(
      user.getId(),
      user.getUsername(),
      user.getFullName(),
      user.getPassword(),
      mapToGrantedAuthorities(user.getRole().getName().name())
    );
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(final String role) {
    return Collections.singletonList(new SimpleGrantedAuthority(role));
  }
}
