package com.shubham.flashsale.ratelimit.dto;

public record RateLimitResult(
    boolean allowed, long limit, long currentCount, long remaining, Long retryAfterMs) {}
