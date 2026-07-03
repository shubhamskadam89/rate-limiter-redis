package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.ratelimit.algorithm.FixedWindowStrategy;
import com.shubham.flashsale.ratelimit.algorithm.SlidingWindowStrategy;
import com.shubham.flashsale.ratelimit.algorithm.TokenBucketStrategy;
import com.shubham.flashsale.ratelimit.identity.DefaultIdentityResolver;
import com.shubham.flashsale.ratelimit.identity.IdentityResolver;
import com.shubham.flashsale.ratelimit.identity.RateLimitIdentity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final TokenBucketStrategy tokenBucketStrategy;
    private final RateLimitProperties properties;
    private final IdentityResolver identityResolver;

    public RateLimitResult checkLimit(HttpServletRequest request) {

        RateLimitIdentity identifier = identityResolver.resolve(request);
        log.debug("Checking rate limit for identifier: {}", identifier);

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