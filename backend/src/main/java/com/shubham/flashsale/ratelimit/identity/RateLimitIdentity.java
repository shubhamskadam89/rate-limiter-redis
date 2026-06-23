package com.shubham.flashsale.ratelimit.identity;

import lombok.Getter;


public record RateLimitIdentity(
        IdentityType type,
        String value
) {
    public String key() {
        return type.name().toLowerCase() + ":" + value;
    }
}