package com.shubham.flashsale.ratelimit.algorithm;

import com.shubham.flashsale.ratelimit.RateLimitProperties;
import com.shubham.flashsale.ratelimit.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TokenBucketStrategyTest {

    private StringRedisTemplate redisTemplate;
    private RedisScript<List> tokenBucketScript;
    private RateLimitProperties properties;

    private TokenBucketStrategy strategy;

    @BeforeEach
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);

        tokenBucketScript = mock(RedisScript.class);

        properties = new RateLimitProperties();
        properties.setBurstCapacity(5);
        properties.setRefillRate(1);

        strategy = new TokenBucketStrategy(
                redisTemplate,
                tokenBucketScript,
                properties
        );
    }

    @Test
    void shouldAllowRequestWhenLuaReturnsAllowed() {

        when(
                redisTemplate.execute(
                        eq(tokenBucketScript),
                        anyList(),
                        anyString(),
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                List.of(1L, 4L)
        );

        RateLimitResult result =
                strategy.checkLimit("demo-user");

        assertTrue(result.allowed());

        assertEquals(
                4,
                result.remaining()
        );

        assertNull(
                result.retryAfterMs()
        );
    }

    @Test
    void shouldBlockRequestWhenLuaReturnsBlocked() {

        when(
                redisTemplate.execute(
                        eq(tokenBucketScript),
                        anyList(),
                        anyString(),
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                List.of(0L, 850L)
        );

        RateLimitResult result =
                strategy.checkLimit("demo-user");

        assertFalse(result.allowed());

        assertEquals(
                0,
                result.remaining()
        );

        assertEquals(
                850L,
                result.retryAfterMs()
        );
    }

    @Test
    void shouldThrowExceptionWhenLuaReturnsNull() {

        when(
                redisTemplate.execute(
                        eq(tokenBucketScript),
                        anyList(),
                        any()
                )
        ).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> strategy.checkLimit("demo-user")
        );
    }

    @Test
    void shouldThrowExceptionWhenLuaReturnsInvalidResponse() {

        when(
                redisTemplate.execute(
                        eq(tokenBucketScript),
                        anyList(),
                        any()
                )
        ).thenReturn(
                List.of(1L)
        );

        assertThrows(
                IllegalStateException.class,
                () -> strategy.checkLimit("demo-user")
        );
    }
}