package com.shubham.flashsale.ratelimit.algorithm;

import com.shubham.flashsale.ratelimit.RateLimitProperties;
import com.shubham.flashsale.ratelimit.RateLimitResult;
import com.shubham.flashsale.ratelimit.RateLimitingStrategy;
import com.shubham.flashsale.ratelimit.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenBucketStrategy  implements RateLimitingStrategy {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> tokenBucketScript;
    private final RateLimitProperties properties;

    @Override
    public RateLimitResult checkLimit(String identifier) {

        String key = RedisKeyBuilder.tokenBucket(identifier);

        List<?> result =
                redisTemplate.execute(
                        tokenBucketScript,
                        List.of(key),
                        String.valueOf(properties.getBurstCapacity()),
                        String.valueOf(properties.getRefillRate()),
                        String.valueOf(System.currentTimeMillis())
                );
        System.out.println("RESULT = " + result);
        if(result==null || result.size()<2){
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
