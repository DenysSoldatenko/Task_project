package com.example.taskmanagerproject.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the Keycloak Admin client.
 */
@Configuration
public class KeycloakConfig {

  @Value("${keycloak.admin-server-url}")
  private String serverUrl;

  @Value("${keycloak.admin-realm}")
  private String realm;

  @Value("${keycloak.admin-client-id}")
  private String clientId;

  @Value("${keycloak.admin-username}")
  private String username;

  @Value("${keycloak.admin-password}")
  private String password;

  /**
   * Creates and configures a {@link Keycloak} instance for admin operations.
   *
   * @return a configured Keycloak admin client
   */
  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
      .serverUrl(serverUrl)
      .realm(realm)
      .username(username)
      .password(password)
      .clientId(clientId)
      .grantType(OAuth2Constants.PASSWORD)
      .build();
  }
}
