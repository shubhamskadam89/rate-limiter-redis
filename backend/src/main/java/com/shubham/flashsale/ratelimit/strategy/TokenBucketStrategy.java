package com.shubham.flashsale.ratelimit.strategy;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketStrategy implements RateLimitingStrategy {

  private final StringRedisTemplate redisTemplate;
  private final RedisScript<List> tokenBucketScript;

  @Override
  public RateLimitResult checkLimit(
      RateLimitIdentity identity, PolicyConfiguration configuration, RateLimitPolicy policy) {
    String key = RedisKeyBuilder.tokenBucket(policy.name(), identity.key());

    double refill =
        configuration.getRefill() != null
            ? configuration.getRefill()
            : (double) configuration.getRequests() / configuration.getWindow();

    List<?> result =
        redisTemplate.execute(
            tokenBucketScript,
            List.of(key),
            String.valueOf(configuration.getBurst()),
            String.valueOf(refill),
            String.valueOf(System.currentTimeMillis()));
    log.debug("Token bucket lua script result: {} for key: {}", result, key);
    if (result == null || result.size() < 2) {
      log.error("Token bucket Lua script returned invalid result for key={}", key);
      throw new IllegalStateException("Lua script returned invalid result");
    }

    long allowed = ((Number) result.get(0)).longValue();

    long value = ((Number) result.get(1)).longValue();
    if (allowed == 1) {
      return new RateLimitResult(
          true,
          configuration.getBurst(),
          0,
          value, // remaining tokens
          null);
    }

    return new RateLimitResult(
        false, configuration.getBurst(), 0, 0, value // retryAfterMs
        );
  }
}
