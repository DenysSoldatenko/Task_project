package com.example.taskmanagerproject.configurations.ratelimiting;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a global Resilience4j RateLimiterRegistry bean.
 */
@Configuration
public class RateLimiterConfigGlobal {

  /**
   * Creates and configures a RateLimiterRegistry bean with default rate limiting settings.
   *
   * @return a RateLimiterRegistry instance with the configured rate limiter
   */
  @Bean
  public RateLimiterRegistry rateLimiterRegistry() {
    RateLimiterConfig config = RateLimiterConfig.custom()
        .limitForPeriod(5)
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .timeoutDuration(Duration.ZERO)
        .build();
    return RateLimiterRegistry.of(config);
  }
}
