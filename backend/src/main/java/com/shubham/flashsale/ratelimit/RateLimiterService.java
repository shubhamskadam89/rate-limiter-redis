package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.ratelimit.algorithm.FixedWindowStrategy;
import com.shubham.flashsale.ratelimit.algorithm.SlidingWindowStrategy;
import com.shubham.flashsale.ratelimit.algorithm.TokenBucketStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final TokenBucketStrategy tokenBucketStrategy;
    private final RateLimitProperties properties;

    public RateLimitResult checkLimit(String identifier) {

        return switch (properties.getAlgorithm()) {

            case FIXED_WINDOW ->
                    fixedWindowStrategy.checkLimit(identifier);

            case SLIDING_WINDOW ->
                    slidingWindowStrategy.checkLimit(identifier);
            case TOKEN_BUCKET ->
                    tokenBucketStrategy.checkLimit(identifier);
        };
    }
}