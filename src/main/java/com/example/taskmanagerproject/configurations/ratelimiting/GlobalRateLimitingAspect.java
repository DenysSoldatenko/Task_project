package com.example.taskmanagerproject.configurations.ratelimiting;

import static com.example.taskmanagerproject.utils.MessageUtil.RATE_LIMIT_EXCEEDED;
import static com.example.taskmanagerproject.utils.MessageUtil.RATE_LIMIT_EXECUTION_ERROR;
import static com.example.taskmanagerproject.utils.MessageUtil.RATE_LIMIT_UNEXPECTED_ERROR;

import com.example.taskmanagerproject.exceptions.RateLimitExceededException;
import com.example.taskmanagerproject.exceptions.RateLimitingExecutionException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for applying global rate limiting to all methods within Spring web controllers.
 */

@Slf4j
@Aspect
@Component
public class GlobalRateLimitingAspect {

  private final RateLimiter rateLimiter;

  /**
   * Constructs a new GlobalRateLimitingAspect.
   * Initializes the RateLimiter by retrieving a pre-configured rate limiter
   * named "globalRateLimiter" from the provided RateLimiterRegistry.
   *
   * @param registry The RateLimiterRegistry containing the rate limiter configurations.
   */
  public GlobalRateLimitingAspect(RateLimiterRegistry registry) {
    this.rateLimiter = registry.rateLimiter("globalRateLimiter");
  }

  /**
   * Applies rate limiting to all methods within classes annotated with @RestController.
   *
   * @param pjp The ProceedingJoinPoint representing the intercepted method execution.
   * @return The result of the original method execution if permitted.
   */
  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  public Object applyRateLimiting(ProceedingJoinPoint pjp) {
    Callable<Object> restrictedCall = RateLimiter.decorateCallable(rateLimiter, () -> {
      try {
        return pjp.proceed();
      } catch (Throwable throwable) {
        throw new RateLimitingExecutionException(RATE_LIMIT_EXECUTION_ERROR, throwable);
      }
    });

    try {
      return restrictedCall.call();
    } catch (RequestNotPermitted ex) {
      log.warn("Rate limit exceeded for request to {}. Message: {}", pjp.getSignature().toShortString(), ex.getMessage());
      throw new RateLimitExceededException(RATE_LIMIT_EXCEEDED);
    } catch (Exception e) {
      log.error("Unexpected error during rate limiting for {}: {}", pjp.getSignature().toShortString(), e.getMessage(), e);
      throw new RateLimitingExecutionException(RATE_LIMIT_UNEXPECTED_ERROR, e);
    }
  }
}
