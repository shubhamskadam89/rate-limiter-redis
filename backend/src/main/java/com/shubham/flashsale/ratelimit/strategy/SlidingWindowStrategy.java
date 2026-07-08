package com.shubham.flashsale.ratelimit.strategy;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlidingWindowStrategy implements RateLimitingStrategy {

  private final StringRedisTemplate redisTemplate;
  private final RedisScript<List> slidingWindowScript;

  @Override
  public RateLimitResult checkLimit(
      RateLimitIdentity identity, PolicyConfiguration configuration, RateLimitPolicy policy) {
    String key = RedisKeyBuilder.slidingWindow(policy.name(), identity.key());

    long now = Instant.now().toEpochMilli();

    long windowStart = now - (configuration.getWindow() * 1000L);

    String member = UUID.randomUUID().toString();

    List<?> result =
        redisTemplate.execute(
            slidingWindowScript,
            List.of(key),
            String.valueOf(windowStart),
            String.valueOf(now),
            member,
            String.valueOf(configuration.getRequests()),
            String.valueOf(configuration.getWindow() * 2L));

    if (result == null || result.size() < 2) {
      log.error("Sliding window Lua script returned invalid result for key={}", key);
      throw new IllegalStateException("Lua script returned invalid result");
    }

    long allowedVal = ((Number) result.get(0)).longValue();
    long count = ((Number) result.get(1)).longValue();

    long remaining = Math.max(0, configuration.getRequests() - count);
    boolean allowed = allowedVal == 1;

    log.debug("Sliding window check key={}, allowed={}, count={}", key, allowed, count);

    return new RateLimitResult(allowed, configuration.getRequests(), count, remaining, null);
  }
}
