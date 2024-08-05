package com.example.taskmanagerproject.configurations;

import static java.util.Locale.ENGLISH;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.example.taskmanagerproject.security.JwtTokenFilter;
import com.example.taskmanagerproject.security.JwtTokenProvider;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Task Manager security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class TaskManagerSecurityConfig {

  private final JwtTokenProvider tokenProvider;

  private static final String[] PUBLIC_ROUTES = {
    "/api/v*/auth/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-resources/**",
    "/swagger-ui.html",
    "/webjars/**",
    "/graphiql"
  };

  /**
   * Bean for password encoding using BCrypt hashing algorithm.
   * This encoder is typically used for securely hashing passwords
   * before storing them in a database.
   *
   * @return a PasswordEncoder instance using BCrypt algorithm.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Bean for generating slugs (URL-friendly strings) from given text.
   * This is typically used to convert a string into a simpler, more readable format,
   * which can be used in URLs, titles, or metadata.
   *
   * @return a Slugify instance configured with English locale.
   */
  @Bean
  public Slugify slugGenerator() {
    return Slugify.builder().locale(ENGLISH).build();
  }

  /**
   * Bean for AuthenticationManager.
   * This bean is responsible for managing authentication requests
   * within the Spring Security framework.
   *
   * @param config the AuthenticationConfiguration object used
   *               to configure the AuthenticationManager.
   * @return the AuthenticationManager bean.
   * @throws Exception if an error occurs while retrieving the AuthenticationManager.
   */
  @Bean
  public AuthenticationManager authenticationManager(
      final AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configures the security filter chain.
   *
   * @param http The HTTP security object.
   * @return The security filter chain.
   * @throws Exception If an error occurs
   *     while configuring the security filter chain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http
  ) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .anonymous(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
          authorizeRequests ->
            authorizeRequests
              .requestMatchers(PUBLIC_ROUTES).permitAll()
              .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .addFilterBefore(
          new JwtTokenFilter(tokenProvider),
          UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
  }
}
