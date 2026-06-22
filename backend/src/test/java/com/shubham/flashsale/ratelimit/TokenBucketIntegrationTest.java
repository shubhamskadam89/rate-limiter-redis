package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.ratelimit.algorithm.TokenBucketStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TokenBucketIntegrationTest {

    @Autowired
    private TokenBucketStrategy strategy;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis() {
        redisTemplate.delete("rate:tb:test-user");
    }

    @Test
    void shouldAllowBurstUpToCapacity() {

        for (int i = 0; i < 5; i++) {
            RateLimitResult result =
                    strategy.checkLimit("test-user");

            assertTrue(result.allowed());
        }

        RateLimitResult blocked =
                strategy.checkLimit("test-user");

        assertFalse(blocked.allowed());
    }
}