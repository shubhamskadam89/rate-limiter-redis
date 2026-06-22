package com.shubham.flashsale.ratelimit;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;


public final class RedisKeyBuilder {

    private RedisKeyBuilder() {}

    public static String fixedWindow(String identifier) {
        return "rate:fw:" + identifier;
    }

    public static String slidingWindow(String identifier) {
        return "rate:sw:" + identifier;
    }
}
