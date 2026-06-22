package com.shubham.flashsale.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SlidingWindowStrategy implements RateLimitingStrategy{

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    @Override
    public RateLimitResult checkLimit(String identifier) {

        String key =
                RedisKeyBuilder.slidingWindow(identifier);

        long now =
                Instant.now().toEpochMilli();

        long windowStart =
                now -
                        (properties.getWindowSeconds() * 1000L);


        // TODO: migrate to newer RedisConnection API when upgrading rate limiter infrastructure
        List<Object> results =
                redisTemplate.executePipelined(
                        (RedisCallback<Object>) connection -> {

                            byte[] redisKey =
                                    key.getBytes();

                            connection.zRemRangeByScore(
                                    redisKey,
                                    0,
                                    windowStart
                            );

                            connection.zAdd(
                                    redisKey,
                                    now,
                                    UUID.randomUUID()
                                            .toString()
                                            .getBytes()
                            );

                            connection.zCard(redisKey);

                            connection.expire(
                                    redisKey,
                                    properties.getWindowSeconds()
                            );

                            return null;
                        }
                );



        long count =
                ((Number) results.get(2))
                        .longValue();

        long remaining = Math.max(0,properties.getMaxRequests()-count);

        boolean allowed = count <= properties.getMaxRequests();




        return new RateLimitResult(
                allowed,
                count,
                remaining
        );
    }

}
