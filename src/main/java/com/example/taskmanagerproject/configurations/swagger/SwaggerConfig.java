package com.example.taskmanagerproject.configurations.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
public class SwaggerConfig {

  private static final String OAUTH2_SCHEME = "KeycloakOAuth";

  @Value("${spring.security.oauth2.authorizationserver.endpoint.authorization-uri}")
  private String authorizationEndpoint;

  @Value("${spring.security.oauth2.authorizationserver.endpoint.token-uri}")
  private String tokenEndpoint;

  /**
   * Configures the OpenAPI documentation with general API info, server,
   * and security schemes (JWT and optional OAuth2).
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
      .info(apiInfo())
      .servers(List.of(new Server().url("http://localhost:8080").description("Local server")))
      .components(new Components()
        .addSecuritySchemes(OAUTH2_SCHEME, keycloakOauthScheme()))
      .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME));
  }

  private Info apiInfo() {
    return new Info()
      .title("Task API")
      .version("2.0")
      .description("Task API Information")
      .termsOfService("https://example.com/terms")
      .license(new License().name("Example License").url("https://example.com"))
      .contact(new Contact()
        .name("Denys Soldatenko")
        .email("john.doe@example.com")
        .url("https://example.com/about"));
  }

  private SecurityScheme keycloakOauthScheme() {
    return new SecurityScheme()
      .type(SecurityScheme.Type.OAUTH2)
      .scheme("bearer")
      .bearerFormat("JWT")
      .flows(new OAuthFlows()
        .authorizationCode(new OAuthFlow()
          .authorizationUrl(authorizationEndpoint)
          .tokenUrl(tokenEndpoint)
          .refreshUrl(tokenEndpoint)));
  }
}
