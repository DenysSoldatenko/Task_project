package com.example.taskmanagerproject.configurations.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
      contact = @Contact(
        name = "Denys Soldatenko",
        email = "john.doe@example.com",
        url = "https://example.com/about"
      ),
      title = "Task API",
      version = "1.0",
      description = "Task API Information",
      license = @License(
        name = "Example License",
        url = "https://example.com"
      ),
      termsOfService = "https://example.com/terms"
    ),
    servers = {
        @Server(
          url = "http://localhost:8080",
          description = "Local server"
        )
    }
)
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {

  /**
   * Configures OpenAPI documentation with
   * security requirements for Bearer Authentication.
   *
   * @return The OpenAPI instance.
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
      .addSecurityItem(
        new SecurityRequirement().addList("Bearer Authentication")
      );
  }
}
