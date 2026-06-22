package com.shubham.flashsale.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final RateLimitProperties properties;

    public RateLimitResult checkLimit(String identifier) {

        return switch (properties.getAlgorithm()) {

            case FIXED_WINDOW ->
                    fixedWindowStrategy.checkLimit(identifier);

            case SLIDING_WINDOW ->
                    slidingWindowStrategy.checkLimit(identifier);
        };
    }
}