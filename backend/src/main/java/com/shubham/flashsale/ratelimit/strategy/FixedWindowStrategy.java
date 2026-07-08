package com.shubham.flashsale.ratelimit.strategy;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedWindowStrategy implements RateLimitingStrategy {

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public RateLimitResult checkLimit(
      RateLimitIdentity identity, PolicyConfiguration configuration, RateLimitPolicy policy) {
    String key = RedisKeyBuilder.fixedWindow(policy.name(), identity.key());

    Long count = stringRedisTemplate.opsForValue().increment(key);

    if (count == null) {
      log.error("Redis increment failed for key={}", key);
      throw new IllegalStateException("Redis increment failed");
    }

    if (count == 1) {
      stringRedisTemplate.expire(key, Duration.ofSeconds(configuration.getWindow()));
    }
    Long remaining = Math.max(0, configuration.getRequests() - count);

    boolean allowed = count <= configuration.getRequests();

    log.debug(
        "Fixed window rate limit check for key={}, count={}, allowed={}", key, count, allowed);

    return new RateLimitResult(allowed, configuration.getRequests(), count, remaining, null);
  }
}
