package com.shubham.flashsale.ratelimit.strategy;

import com.shubham.flashsale.ratelimit.config.RateLimitAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyFactory {

  private final FixedWindowStrategy fixedWindowStrategy;
  private final SlidingWindowStrategy slidingWindowStrategy;
  private final TokenBucketStrategy tokenBucketStrategy;

  public RateLimitingStrategy getStrategy(RateLimitAlgorithm algorithm) {
    return switch (algorithm) {
      case FIXED_WINDOW -> fixedWindowStrategy;
      case SLIDING_WINDOW -> slidingWindowStrategy;
      case TOKEN_BUCKET -> tokenBucketStrategy;
    };
  }
}
