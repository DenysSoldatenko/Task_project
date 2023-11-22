package com.example.taskmanagerproject.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a user for JWT authentication.
 */
@Data
@Builder
public class JwtUser implements UserDetails {

  @JsonIgnore
  private final Long id;

  private final String username;

  private final String fullName;

  private final String password;

  private final Collection<? extends GrantedAuthority> authorities;

  @JsonIgnore
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @JsonIgnore
  @Override
  public boolean isEnabled() {
    return true;
  }
}