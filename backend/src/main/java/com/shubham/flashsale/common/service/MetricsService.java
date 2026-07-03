package com.shubham.flashsale.common.service;

import com.shubham.flashsale.config.MetricsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MetricsConfig metricsConfig;

    public void incrementRateLimitBreach() {
        log.debug("Incrementing rate limit breach metric");
        metricsConfig.getRateLimitBreachesCounter().increment();
    }

    public void incrementInventoryDecrement() {
        log.debug("Incrementing inventory decrement metric");
        metricsConfig.getInventoryDecrementsCounter().increment();
    }

    public void incrementIdempotencyHit() {
        log.debug("Incrementing idempotency hit metric");
        metricsConfig.getIdempotencyHitsCounter().increment();
    }
}