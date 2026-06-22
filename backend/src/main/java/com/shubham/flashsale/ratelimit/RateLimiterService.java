package com.shubham.flashsale.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitProperties rateLimitProperties;



    public RateLimitResult checkLimit(String identifier){
        String key = RedisKeyBuilder.fixedWindow(identifier);

        Long count  =  stringRedisTemplate
                .opsForValue()
                .increment(key);

        if(count==null){
            throw new IllegalStateException("Redis increment failed");
        }

        if(count==1){
            stringRedisTemplate.expire(key,
                    Duration.ofSeconds(rateLimitProperties.getWindowSeconds()));
        }
        Long remaining  = Math.max(0,
                rateLimitProperties.getMaxRequests()-count);

        boolean allowed  = count<= rateLimitProperties.getMaxRequests();

        return new RateLimitResult(allowed,count,remaining);
    }




    

}
