package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.User;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
  public static JwtEntity create(User user) {
    return new JwtEntity(
      user.getId(),
      user.getUsername(),
      user.getFullName(),
      user.getPassword(),
      mapToGrantedAuthorities(user.getRole().getName())
    );
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(String role) {
    return Collections.singletonList(new SimpleGrantedAuthority(role));
  }
}
