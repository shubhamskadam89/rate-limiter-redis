package com.shubham.flashsale.ratelimit.algorithm;

import com.shubham.flashsale.ratelimit.RateLimitProperties;
import com.shubham.flashsale.ratelimit.RateLimitResult;
import com.shubham.flashsale.ratelimit.RateLimitingStrategy;
import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.identity.RateLimitIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class SlidingWindowStrategy implements RateLimitingStrategy {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> slidingWindowScript;
    private final RateLimitProperties properties;

    @Override
    public RateLimitResult checkLimit(RateLimitIdentity identifier) {
        String key =
                RedisKeyBuilder.slidingWindow(
                        identifier.key()
                );

        long now =
                Instant.now().toEpochMilli();

        long windowStart =
                now -
                        (properties.getWindowSeconds() * 1000L);

        String member = UUID.randomUUID().toString();

        List<?> result = redisTemplate.execute(
                slidingWindowScript,
                List.of(key),
                String.valueOf(windowStart),
                String.valueOf(now),
                member,
                String.valueOf(properties.getMaxRequests()),
                String.valueOf(properties.getWindowSeconds() * 2L)
        );

        if (result == null || result.size() < 2) {
            log.error("Sliding window Lua script returned invalid result for key={}", key);
            throw new IllegalStateException("Lua script returned invalid result");
        }

        long allowedVal = ((Number) result.get(0)).longValue();
        long count = ((Number) result.get(1)).longValue();

        long remaining = Math.max(0, properties.getMaxRequests() - count);
        boolean allowed = allowedVal == 1;

        log.debug("Sliding window check key={}, allowed={}, count={}", key, allowed, count);

        return new RateLimitResult(
                allowed,
                count,
                remaining,
                null
        );
    }

}
