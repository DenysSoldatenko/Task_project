package com.example.taskmanagerproject.security;

import static java.util.stream.Collectors.toList;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Factory class for creating JwtEntity objects.
 */
@UtilityClass
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
      mapToGrantedAuthorities(new ArrayList<>(user.getUserRoles()))
    );
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(
      final List<Role> roles
  ) {
    return roles.stream()
      .map(Enum::name)
      .map(SimpleGrantedAuthority::new)
      .collect(toList());
  }
}
