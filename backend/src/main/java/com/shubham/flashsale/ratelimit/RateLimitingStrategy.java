package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.ratelimit.identity.RateLimitIdentity;

public interface RateLimitingStrategy {

    RateLimitResult checkLimit(RateLimitIdentity identifier);
}