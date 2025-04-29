package com.example.taskmanagerproject.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskmanagerproject.dtos.users.UserDto;
import com.example.taskmanagerproject.services.AuthenticationService;
import com.example.taskmanagerproject.services.UserService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for {@link AuthenticationController}, achieving full line coverage.
 *
 * <p>These tests verify standard behavior and basic error handling for user registration
 * and authentication-related endpoints. Mocked services are used to isolate the controller layer.
 *
 * <p><strong>Limitations and Technical Debt:</strong></p>
 * The current test suite does not yet cover the following edge cases and exceptional scenarios:
 * <ul>
 *   <li>JWT tokens with missing or malformed claims</li>
 *   <li>Expired or invalid JWT tokens (authentication/authorization errors)</li>
 *   <li>Null or improperly constructed {@code UserDto} instances</li>
 *   <li>Unauthorized or forbidden access attempts (e.g. missing roles or authorities)</li>
 *   <li>Domain-specific exceptions such as {@code EntityNotFoundException}</li>
 * </ul>
 *
 * <p>Consider splitting certain edge case tests into lower-level unit tests for more
 * precise control over mocking and exception simulation.</p>
 */
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private UserService userService;

  @MockBean
  private AuthenticationService authenticationService;

  private Jwt validJwt;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webApplicationContext)
      .apply(springSecurity())
      .build();
    userDto = new UserDto(1L, "Test User", "user@example.com", "test-user", "", null);
    validJwt = createValidJwt();
  }

  private Jwt createValidJwt() {
    return Jwt.withTokenValue("valid-token")
      .header("alg", "HS256")
      .issuer("https://issuer.example.com")
      .subject("user@example.com")
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plusSeconds(3600))
      .claim("email", "user@example.com")
      .claim("fullName", "Test User")
      .build();
  }

  @Nested
  @DisplayName("Register User Tests")
  class RegisterUserTests {

    @Test
    void shouldReturn201AndCreatedUser() throws Exception {
      when(userService.createUser(any())).thenReturn(userDto);

      mockMvc.perform(post("/api/v2/auth")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(1L))
          .andExpect(jsonPath("$.username").value("user@example.com"))
          .andExpect(jsonPath("$.fullName").value("Test User"))
          .andExpect(jsonPath("$.slug").value("test-user"));

      verify(userService).createUser(any());
      verifyNoMoreInteractions(userService, authenticationService);
    }

    @Test
    void shouldReturn500WhenCreateUserFails() throws Exception {
      when(userService.createUser(any())).thenThrow(new RuntimeException("Internal server error"));

      mockMvc.perform(post("/api/v2/auth")
          .with(jwt().jwt(validJwt).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message").value("Internal server error"))
          .andExpect(jsonPath("$.status").value(500))
          .andExpect(jsonPath("$.error").value("Internal Server Error"));

      verify(userService).createUser(any());
      verifyNoMoreInteractions(userService, authenticationService);
    }
  }

  @Nested
  @DisplayName("User Info Tests")
  class UserInfoTests {

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn200AndUserInfo() throws Exception {
      String info = "User: user@example.com, Claims: {email=user@example.com}";
      when(authenticationService.logAuthenticationInfo(any())).thenReturn(info);

      mockMvc.perform(get("/api/v2/auth/user-info"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andExpect(content().string(containsString("user@example.com")));

      verify(authenticationService).logAuthenticationInfo(any());
      verifyNoMoreInteractions(userService, authenticationService);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void shouldReturn500IfAuthenticationServiceFails() throws Exception {
      when(authenticationService.logAuthenticationInfo(any())).thenThrow(new RuntimeException("Internal server error"));

      mockMvc.perform(get("/api/v2/auth/user-info"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.message").value("Internal server error"))
          .andExpect(jsonPath("$.status").value(500))
          .andExpect(jsonPath("$.error").value("Internal Server Error"));

      verify(authenticationService).logAuthenticationInfo(any());
      verifyNoMoreInteractions(userService, authenticationService);
    }
  }
}