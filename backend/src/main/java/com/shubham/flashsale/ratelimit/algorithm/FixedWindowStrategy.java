package com.shubham.flashsale.ratelimit.algorithm;

import com.shubham.flashsale.ratelimit.RateLimitProperties;
import com.shubham.flashsale.ratelimit.RateLimitResult;
import com.shubham.flashsale.ratelimit.RateLimitingStrategy;
import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.ratelimit.identity.RateLimitIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedWindowStrategy implements RateLimitingStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitProperties rateLimitProperties;



    @Override
    public RateLimitResult checkLimit(RateLimitIdentity identifier){
        String key =
                RedisKeyBuilder.fixedWindow(
                        identifier.key()
                );

        Long count  =  stringRedisTemplate
                .opsForValue()
                .increment(key);

        if(count==null){
            log.error("Redis increment failed for key={}", key);
            throw new IllegalStateException("Redis increment failed");
        }

        if(count==1){
            stringRedisTemplate.expire(key,
                    Duration.ofSeconds(rateLimitProperties.getWindowSeconds()));
        }
        Long remaining  = Math.max(0,
                rateLimitProperties.getMaxRequests()-count);

        boolean allowed  = count<= rateLimitProperties.getMaxRequests();

        log.debug("Fixed window rate limit check for key={}, count={}, allowed={}", key, count, allowed);

        return new RateLimitResult(allowed,
                count,
                remaining,
                null);
    }




    

}
