package com.shubham.flashsale.ratelimit.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.IdentityType;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class TokenBucketIntegrationTest {

  @Autowired private TokenBucketStrategy strategy;

  @Autowired private StringRedisTemplate redisTemplate;

  @BeforeEach
  void cleanRedis() {
    redisTemplate.delete(
        RedisKeyBuilder.tokenBucket(RateLimitPolicy.TRANSACTION.name(), "USER:test-user"));
  }

  @Test
  void shouldAllowBurstUpToCapacity() {
    PolicyConfiguration config = new PolicyConfiguration();
    config.setBurst(5L);
    config.setRefill(1.0);

    for (int i = 0; i < 5; i++) {
      RateLimitResult result =
          strategy.checkLimit(
              new RateLimitIdentity(IdentityType.USER, "test-user"),
              config,
              RateLimitPolicy.TRANSACTION);

      assertTrue(result.allowed());
    }

    RateLimitResult blocked =
        strategy.checkLimit(
            new RateLimitIdentity(IdentityType.USER, "test-user"),
            config,
            RateLimitPolicy.TRANSACTION);

    assertFalse(blocked.allowed());
  }
}
