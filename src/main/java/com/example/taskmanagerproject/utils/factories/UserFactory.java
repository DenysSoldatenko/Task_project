package com.example.taskmanagerproject.utils.factories;

import static com.example.taskmanagerproject.utils.MessageUtil.KEYCLOAK_ERROR_FAILED_TO_CREATE_USER;
import static com.example.taskmanagerproject.utils.MessageUtil.KEYCLOAK_ERROR_GENERIC_CREATION;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import com.example.taskmanagerproject.exceptions.KeycloakUserCreationException;
import com.github.slugify.Slugify;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating User instances and managing Keycloak user creation from registration requests.
 */
@Slf4j
@Component
public final class UserFactory {

  @Value("${keycloak.target-realm}")
  private String keycloakRealm;

  private final Keycloak keycloak;
  private final Slugify slugGenerator;

  /**
   * Constructs a {@code UserFactory} with the given slug generator and Keycloak client.
   *
   * @param slugGenerator the slug generator used to create unique slugs
   * @param keycloak the Keycloak client for user management operations
   */
  public UserFactory(Slugify slugGenerator, Keycloak keycloak) {
    this.slugGenerator = slugGenerator;
    this.keycloak = keycloak;
  }

  /**
   * Creates a user in Keycloak and returns the corresponding local User entity.
   *
   * @param request DTO with registration data.
   * @return Local User entity without password.
   * @throws KeycloakUserCreationException if Keycloak creation fails.
   */
  public User createUserFromRequest(UserDto request) {
    log.info("Creating user '{}' in Keycloak for realm '{}'.", request.username(), keycloakRealm);

    UserRepresentation kcUser = mapToKeycloakUser(request);

    try (Response response = keycloak.realm(keycloakRealm).users().create(kcUser)) {
      handleKeycloakResponse(response, request.username());
    } catch (Exception e) {
      log.error("Unexpected error creating user '{}': {}", request.username(), e.getMessage(), e);
      throw new KeycloakUserCreationException(format(KEYCLOAK_ERROR_GENERIC_CREATION, request.username(), e));
    }

    return buildLocalUser(request);
  }

  private UserRepresentation mapToKeycloakUser(UserDto request) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.password());
    credential.setTemporary(false);

    UserRepresentation user = new UserRepresentation();
    user.setUsername(request.username());
    user.setEmail(request.username());
    user.setEnabled(true);
    user.setCredentials(List.of(credential));

    return user;
  }

  private void handleKeycloakResponse(Response response, String username) {
    int status = response.getStatus();
    if (status == 201) {
      log.info("Successfully created user '{}' in Keycloak.", username);
    } else {
      String errorBody = response.readEntity(String.class);
      log.error("Failed to create user '{}'. Status: {}, Error: {}", username, status, errorBody);
      throw new KeycloakUserCreationException(format(KEYCLOAK_ERROR_FAILED_TO_CREATE_USER, status, errorBody));
    }
  }

  private User buildLocalUser(UserDto request) {
    return User.builder()
      .fullName(request.fullName())
      .username(request.username())
      .slug(generateSlug(request.fullName()))
      .build();
  }

  private String generateSlug(String fullName) {
    return slugGenerator.slugify(fullName) + "-" + randomUUID().toString().substring(0, 8);
  }
}
