package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.JWT_MISSING_REQUIRED_CLAIMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.KeycloakUserCreationException;
import com.example.taskmanagerproject.exceptions.ValidationException;
import com.github.slugify.Slugify;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserFactoryTest {

  private Slugify slugify;
  private UserFactory factory;
  private UsersResource usersResource;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    Keycloak keycloak = mock(Keycloak.class);
    slugify = mock(Slugify.class);

    RealmResource realmResource = mock(RealmResource.class);
    usersResource = mock(UsersResource.class);

    when(keycloak.realm("test-realm")).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);

    factory = new UserFactory(slugify, keycloak);

    // Inject the value of keycloakRealm via reflection
    Field realmField = UserFactory.class.getDeclaredField("keycloakRealm");
    realmField.setAccessible(true);
    realmField.set(factory, "test-realm");
  }

  @Test
  void shouldCreateUserFromJwt() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaim("email")).thenReturn("user@example.com");
    when(jwt.getClaim("name")).thenReturn("John Doe");

    when(slugify.slugify("John Doe")).thenReturn("john-doe");

    JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
    when(token.getToken()).thenReturn(jwt);

    User user = factory.createUserFromRequest(token);

    assertEquals("user@example.com", user.getUsername());
    assertEquals("John Doe", user.getFullName());
    assertTrue(user.getSlug().startsWith("john-doe-"));
  }

  @Test
  void shouldThrowValidationExceptionWhenJwtMissingClaims() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaim("email")).thenReturn(null);

    JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
    when(token.getToken()).thenReturn(jwt);

    ValidationException ex = assertThrows(ValidationException.class, () -> factory.createUserFromRequest(token));
    assertEquals(JWT_MISSING_REQUIRED_CLAIMS, ex.getMessage());
  }

  @Test
  void shouldCreateUserInKeycloakAndReturnLocalUser() {
    UserDto dto = new UserDto(1L, "John Doe", "user@example.com", null, "secret", List.of());
    when(slugify.slugify("John Doe")).thenReturn("john-doe");

    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(201);
    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

    User user = factory.createUserFromRequest(dto);

    assertEquals("user@example.com", user.getUsername());
    assertEquals("John Doe", user.getFullName());
    assertTrue(user.getSlug().startsWith("john-doe-"));
  }

  @Test
  void shouldThrowKeycloakUserCreationExceptionIfResponseNot201() {
    UserDto dto = new UserDto(1L, "user@example.com", "John Doe", null, "secret", List.of());
    when(slugify.slugify("John Doe")).thenReturn("john-doe");

    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(409);
    when(response.readEntity(String.class)).thenReturn("User already exists");
    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

    KeycloakUserCreationException ex = assertThrows(KeycloakUserCreationException.class, () -> factory.createUserFromRequest(dto));
    assertTrue(ex.getMessage().contains("User already exists"));
  }

  @Test
  void shouldThrowKeycloakUserCreationExceptionOnUnexpectedError() {
    UserDto dto = new UserDto(1L, "user@example.com", "John Doe", null, "secret", List.of());
    when(slugify.slugify("John Doe")).thenReturn("john-doe");

    when(usersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Unexpected error"));

    KeycloakUserCreationException ex = assertThrows(KeycloakUserCreationException.class, () -> factory.createUserFromRequest(dto));
    assertTrue(ex.getMessage().contains("Unexpected error"));
  }
}
