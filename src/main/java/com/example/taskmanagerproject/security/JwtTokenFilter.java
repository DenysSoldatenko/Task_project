package com.example.taskmanagerproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A filter for processing JWT tokens in incoming requests
 * and setting the authentication context.
 */
@Component
@AllArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void doFilter(ServletRequest req,
                       ServletResponse res,
                       FilterChain filterChain) throws IOException, ServletException {

    String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
    if (token != null && jwtTokenProvider.validateToken(token)) {
      Authentication auth = jwtTokenProvider.getAuthentication(token);

      if (auth != null) {
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    filterChain.doFilter(req, res);
  }
}