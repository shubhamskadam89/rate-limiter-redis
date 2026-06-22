package com.shubham.flashsale.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    private RateLimitAlgorithm algorithm;
    private long maxRequests;
    private long windowSeconds;
    private long burstCapacity;
    private double refillRate;
}