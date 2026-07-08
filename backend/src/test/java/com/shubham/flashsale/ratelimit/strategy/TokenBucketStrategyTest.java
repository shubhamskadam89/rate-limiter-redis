package com.shubham.flashsale.ratelimit.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shubham.flashsale.ratelimit.config.PolicyConfiguration;
import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.identity.IdentityType;
import com.shubham.flashsale.ratelimit.resolver.identity.RateLimitIdentity;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

class TokenBucketStrategyTest {

  private StringRedisTemplate redisTemplate;
  private RedisScript<List> tokenBucketScript;
  private PolicyConfiguration config;
  private TokenBucketStrategy strategy;

  @BeforeEach
  void setUp() {
    redisTemplate = mock(StringRedisTemplate.class);
    tokenBucketScript = mock(RedisScript.class);

    config = new PolicyConfiguration();
    config.setBurst(5L);
    config.setRefill(1.0);

    strategy = new TokenBucketStrategy(redisTemplate, tokenBucketScript);
  }

  @Test
  void shouldAllowRequestWhenLuaReturnsAllowed() {
    when(redisTemplate.execute(
            eq(tokenBucketScript), anyList(), anyString(), anyString(), anyString()))
        .thenReturn(List.of(1L, 4L));

    RateLimitResult result =
        strategy.checkLimit(
            new RateLimitIdentity(IdentityType.USER, "demo-user"), config, RateLimitPolicy.GENERAL);

    assertTrue(result.allowed());
    assertEquals(4, result.remaining());
    assertNull(result.retryAfterMs());
  }

  @Test
  void shouldBlockRequestWhenLuaReturnsBlocked() {
    when(redisTemplate.execute(
            eq(tokenBucketScript), anyList(), anyString(), anyString(), anyString()))
        .thenReturn(List.of(0L, 850L));

    RateLimitResult result =
        strategy.checkLimit(
            new RateLimitIdentity(IdentityType.USER, "demo-user"), config, RateLimitPolicy.GENERAL);

    assertFalse(result.allowed());
    assertEquals(0, result.remaining());
    assertEquals(850L, result.retryAfterMs());
  }

  @Test
  void shouldThrowExceptionWhenLuaReturnsNull() {
    when(redisTemplate.execute(eq(tokenBucketScript), anyList(), any())).thenReturn(null);

    assertThrows(
        IllegalStateException.class,
        () ->
            strategy.checkLimit(
                new RateLimitIdentity(IdentityType.USER, "demo-user"),
                config,
                RateLimitPolicy.GENERAL));
  }

  @Test
  void shouldThrowExceptionWhenLuaReturnsInvalidResponse() {
    when(redisTemplate.execute(eq(tokenBucketScript), anyList(), any())).thenReturn(List.of(1L));

    assertThrows(
        IllegalStateException.class,
        () ->
            strategy.checkLimit(
                new RateLimitIdentity(IdentityType.USER, "demo-user"),
                config,
                RateLimitPolicy.GENERAL));
  }
}
