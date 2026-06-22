package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.ratelimit.algorithm.SlidingWindowStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final RateLimiterService rateLimiterService;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/limit")
    public RateLimitResult test() {

        return rateLimiterService
                .checkLimit("demo-user");
    }

    @GetMapping("/sw")
    public RateLimitResult testSliding() {
        return rateLimiterService
                .checkLimit("demo-user");
    }


    @GetMapping("/pipeline")
    public String pipelineTest(){
        List<Object> results =
                redisTemplate.executePipelined(
                        (RedisCallback<Object>) connection -> {
                            byte[] key =
                                    "pipeline:test".getBytes();

                            connection.zAdd(
                                    key,
                                    System.currentTimeMillis(),
                                    UUID.randomUUID()
                                            .toString()
                                            .getBytes()
                            );

                            connection.zCard(key);

                            connection.expire(key, 60);

                            return null;
                        }
                );
        return results.toString();
    }

}