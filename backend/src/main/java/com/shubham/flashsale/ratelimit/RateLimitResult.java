package com.shubham.flashsale.ratelimit;

import lombok.Getter;


public record RateLimitResult(
        boolean allowed,
        long currentCount,
        long remainingRequests
) {
}
