package com.shubham.flashsale.ratelimit.strategy;

import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;

public interface RateLimitingStrategy {

  RateLimitResult checkLimit(
      RateLimitIdentity identity, PolicyConfiguration configuration, RateLimitPolicy policy);
}
