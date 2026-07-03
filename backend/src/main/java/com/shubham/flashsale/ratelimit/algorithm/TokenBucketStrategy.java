package com.shubham.flashsale.ratelimit.algorithm;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.*;
import com.shubham.flashsale.ratelimit.identity.RateLimitIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketStrategy  implements RateLimitingStrategy {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> tokenBucketScript;
    private final RateLimitProperties properties;

    @Override
    public RateLimitResult checkLimit(RateLimitIdentity identifier) {
        String key =
                RedisKeyBuilder.tokenBucket(
                        identifier.key()
                );

        List<?> result =
                redisTemplate.execute(
                        tokenBucketScript,
                        List.of(key),
                        String.valueOf(properties.getBurstCapacity()),
                        String.valueOf(properties.getRefillRate()),
                        String.valueOf(System.currentTimeMillis())
                );
        log.debug("Token bucket lua script result: {} for key: {}", result, key);
        if(result==null || result.size()<2){
            log.error("Token bucket Lua script returned invalid result for key={}", key);
            throw new IllegalStateException(
                    "Lua script returned invalid result"
            );
        }

        long allowed  = ((Number) result.get(0)).longValue();

        long value = ((Number) result.get(1)).longValue();
        if (allowed == 1) {
            return new RateLimitResult(
                    true,
                    0,
                    value,      // remaining tokens
                    null
            );
        }

        return new RateLimitResult(
                false,
                0,
                0,
                value          // retryAfterMs
        );
    }
}
