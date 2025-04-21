package com.example.taskmanagerproject.utils.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.users.RoleDto;
import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.Role;
import com.example.taskmanagerproject.entities.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserMapperTest {

  @Mock
  private RoleMapper roleMapper;

  @InjectMocks
  private UserMapperImpl userMapper;

  private User user;
  private UserDto userDto;

  private Role role;
  private RoleDto roleDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    role = new Role();
    role.setName("MEMBER");

    roleDto = new RoleDto(100L, "MEMBER", "Team member role");

    user = new User();
    user.setId(1L);
    user.setUsername("user@gmail.com");
    user.setFullName("User");
    user.setSlug("user-slug");

    userDto = new UserDto(1L, "User", "user@gmail.com", "user-slug", null, null);
  }

  @Test
  void shouldMapUserToDto() {
    when(roleMapper.toDto(role)).thenReturn(roleDto);

    UserDto result = userMapper.toDto(user);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("user@gmail.com", result.username());
    assertEquals("User", result.fullName());
    assertEquals("user-slug", result.slug());
    assertNull(result.password());
  }

  @Test
  void shouldMapUserDtoToEntity() {
    when(roleMapper.toEntity(roleDto)).thenReturn(role);

    User result = userMapper.toEntity(userDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("user@gmail.com", result.getUsername());
    assertEquals("User", result.getFullName());
    assertEquals("user-slug", result.getSlug());
  }

  @Test
  void shouldHandleNullUser() {
    UserDto result = userMapper.toDto(null);
    assertNull(result);
  }

  @Test
  void shouldHandleNullUserDto() {
    User result = userMapper.toEntity(null);
    assertNull(result);
  }
}