package com.shubham.flashsale.ratelimit.service;

import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.config.RateLimitProperties;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.IdentityResolver;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import com.shubham.flashsale.ratelimit.strategy.RateLimitingStrategy;
import com.shubham.flashsale.ratelimit.strategy.StrategyFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

  private final StrategyFactory strategyFactory;
  private final RateLimitProperties properties;
  private final IdentityResolver identityResolver;

  public RateLimitResult checkLimit(HttpServletRequest request, RateLimitPolicy policy) {
    RateLimitIdentity identity = identityResolver.resolve(request);
    PolicyConfiguration configuration = getConfiguration(policy);
    RateLimitingStrategy strategy = strategyFactory.getStrategy(configuration.getAlgorithm());

    log.debug("Checking rate limit for identity: {}, policy: {}", identity, policy);
    return strategy.checkLimit(identity, configuration, policy);
  }

  private PolicyConfiguration getConfiguration(RateLimitPolicy policy) {
    PolicyConfiguration configuration = properties.getPolicies().get(policy);
    if (configuration == null) {
      throw new IllegalStateException("No rate limit configuration found for policy " + policy);
    }
    return configuration;
  }
}
