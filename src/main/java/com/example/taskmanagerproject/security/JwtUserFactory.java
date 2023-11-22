package com.example.taskmanagerproject.security;

import com.example.taskmanagerproject.entities.Role;
import com.example.taskmanagerproject.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Factory for creating JwtUser instances from User entities.
 */
@NoArgsConstructor
public final class JwtUserFactory {

  /**
   * Creates a JwtUser instance from a User entity.
   *
   * @param user The User entity.
   * @return JwtUser instance created from the User entity.
   */
  public static JwtUser create(User user) {
    return JwtUser.builder()
    .id(user.getId())
    .username(user.getUsername())
    .fullName(user.getFullName())
    .password(user.getPassword())
    .authorities(mapToGrantedAuthorities(new ArrayList<>(user.getUserRoles())))
    .build();
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> userRoles) {
    return userRoles.stream()
    .map(role -> new SimpleGrantedAuthority(role.name()))
    .collect(Collectors.toList());
  }
}