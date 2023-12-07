package com.example.taskmanagerproject.configurations;

import com.example.taskmanagerproject.security.JwtTokenFilter;
import com.example.taskmanagerproject.security.JwtTokenProvider;
import io.minio.MinioClient;
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
import org.springframework.security.config.http.SessionCreationPolicy;
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
  private final MinioProperties minioProperties;

  private static final String[] PUBLIC_ROUTES = {
    "/api/v*/auth/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-resources/**",
    "/swagger-ui.html",
    "/webjars/**"
  };

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Creates a MinioClient bean to interact with MinIO server.
   *
   * @return A MinioClient instance.
   */
  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder()
      .endpoint(minioProperties.getUrl())
      .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
      .build();
  }

  /**
   * Configures the security filter chain.
   *
   * @param http The HTTP security object.
   * @return The security filter chain.
   * @throws Exception If an error occurs while configuring the security filter chain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
        .sessionManagement(session -> session
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(new JwtTokenFilter(tokenProvider),
          UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
  }
}
