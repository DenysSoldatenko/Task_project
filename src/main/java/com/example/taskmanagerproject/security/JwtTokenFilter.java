package com.example.taskmanagerproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A filter for processing JWT tokens in incoming requests
 * and setting the authentication context.
 */
@AllArgsConstructor
public final class JwtTokenFilter extends GenericFilterBean {

  private static final int BEARER_TOKEN_PREFIX_LENGTH = 7;

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  @SneakyThrows
  public void doFilter(final ServletRequest servletRequest,
                       final ServletResponse servletResponse,
                       final FilterChain filterChain) {
    String bearerToken = extractBearerToken(
        (HttpServletRequest) servletRequest
    );
    if (bearerToken != null && jwtTokenProvider.validateToken(bearerToken)) {
      Authentication authentication = jwtTokenProvider
          .getAuthentication(bearerToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }

  private String extractBearerToken(final HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(BEARER_TOKEN_PREFIX_LENGTH);
    }
    return null;
  }
}
