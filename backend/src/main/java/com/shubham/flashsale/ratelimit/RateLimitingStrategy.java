package com.shubham.flashsale.ratelimit;

public interface RateLimitingStrategy {

    RateLimitResult checkLimit(String identifier);
}