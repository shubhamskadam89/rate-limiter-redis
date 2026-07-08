package com.shubham.flashsale.ratelimit.web;

import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import com.shubham.flashsale.ratelimit.service.RateLimiterService;
import com.shubham.flashsale.ratelimit.strategy.SlidingWindowStrategy;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

  private final RateLimiterService rateLimiterService;
  private final SlidingWindowStrategy slidingWindowStrategy;
  private final StringRedisTemplate redisTemplate;

  @GetMapping("/limit")
  public RateLimitResult test(HttpServletRequest request) {

    return rateLimiterService.checkLimit(request, RateLimitPolicy.GENERAL);
  }

  @GetMapping("/public/limit")
  public RateLimitResult test2(HttpServletRequest request) {
    return rateLimiterService.checkLimit(request, RateLimitPolicy.GENERAL);
  }

  @GetMapping("/pipeline")
  public String pipelineTest() {
    List<Object> results =
        redisTemplate.executePipelined(
            (RedisCallback<Object>)
                connection -> {
                  byte[] key = "pipeline:test".getBytes(StandardCharsets.UTF_8);

                  connection.zAdd(
                      key,
                      System.currentTimeMillis(),
                      UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

                  connection.zCard(key);

                  connection.expire(key, 60);

                  return null;
                });
    return results.toString();
  }
}
