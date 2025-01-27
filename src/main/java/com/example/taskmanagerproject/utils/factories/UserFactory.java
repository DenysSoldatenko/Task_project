package com.example.taskmanagerproject.utils.factories;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.entities.users.User;
import com.github.slugify.Slugify;
import jakarta.ws.rs.core.Response;
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

  private final Slugify slugGenerator;
  private final Keycloak keycloak;

  @Value("${keycloak.target-realm}")
  private String keycloakRealm;

  public UserFactory(Slugify slugGenerator, Keycloak keycloak) {
    this.slugGenerator = slugGenerator;
    this.keycloak = keycloak;
  }
// todo add custom exception + constant
  /**
   * Creates a user in Keycloak and returns a corresponding local User entity (without password).
   *
   * @param request DTO containing user registration details.
   * @return A local User entity with full name, username, and a generated slug.
   * @throws RuntimeException if user creation in Keycloak fails.
   */
  public User createUserFromRequest(UserDto request) {
    log.info("Attempting to create user '{}' in Keycloak for realm '{}'.", request.username(), keycloakRealm);

    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(request.username());
    kcUser.setEmail(request.username());
    kcUser.setEnabled(true);

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.password());
    kcUser.setCredentials(singletonList(credential));

    try (Response response = keycloak.realm(keycloakRealm).users().create(kcUser)) {
      if (response.getStatus() == 201) {
        log.info("Successfully created user '{}' in Keycloak. Status: {}", request.username(), response.getStatus());
      } else {
        String errorMessage = response.readEntity(String.class);
        log.error("Failed to create user '{}' in Keycloak. Status: {}. Error: {}", request.username(), response.getStatus(), errorMessage);
        throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus() + ". Details: " + errorMessage);
      }
    } catch (Exception e) {
      log.error("An unexpected error occurred during Keycloak user creation for '{}': {}", request.username(), e.getMessage(), e);
      throw new RuntimeException("An error occurred during Keycloak user creation for " + request.username(), e);
    }

    return User.builder()
      .fullName(request.fullName())
      .username(request.username())
      .slug(generateSlug(request.fullName()))
      .build();
  }

  /**
   * Generates a unique slug from a full name.
   *
   * @param fullName The full name to slugify.
   * @return A unique slug.
   */
  private String generateSlug(String fullName) {
    return slugGenerator.slugify(fullName) + "-" + randomUUID().toString().substring(0, 8);
  }
}