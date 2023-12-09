package com.example.taskmanagerproject.configurations;

import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration class for GraphQL.
 */
@Configuration
public class GraphqlConfig {

  /**
   * Defines a GraphQL scalar type for LocalDateTime.
   *
   * @return The GraphQLScalarType for LocalDateTime.
   */
  @Bean
  public GraphQLScalarType localDateTimeScalar() {
    return GraphQLScalarType.newScalar()
      .name("LocalDateTime")
      .description("LocalDateTime scalar")
      .coercing(new LocalDateTimeCoercing())
      .build();
  }

  /**
   * Configures runtime wiring for GraphQL.
   *
   * @return The RuntimeWiringConfigurer.
   */
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder
      .scalar(localDateTimeScalar())
      .build();
  }
}
