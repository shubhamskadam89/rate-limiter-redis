package com.shubham.flashsale.ratelimit.web;

import com.shubham.flashsale.common.service.MetricsService;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicyResolver;
import com.shubham.flashsale.ratelimit.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

  private final RateLimitPolicyResolver policyResolver;
  private final RateLimiterService rateLimiterService;
  private final MetricsService metricsService;
  private final RateLimitResponseWriter responseWriter;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {

    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    RateLimitPolicy policy = policyResolver.resolve(handlerMethod);
    RateLimitResult result = rateLimiterService.checkLimit(request, policy);

    if (!result.allowed()) {
      log.warn(
          "Rate limit breached for request URI={}, IP={}, policy={}",
          request.getRequestURI(),
          request.getRemoteAddr(),
          policy);

      metricsService.incrementRateLimitBreach();
      responseWriter.write(response, result);
      return false;
    }

    log.debug("Rate limit passed for URI={}, policy={}", request.getRequestURI(), policy);
    return true;
  }
}
