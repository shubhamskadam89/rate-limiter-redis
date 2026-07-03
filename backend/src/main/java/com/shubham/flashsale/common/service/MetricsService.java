package com.shubham.flashsale.common.service;

import com.shubham.flashsale.config.MetricsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MetricsConfig metricsConfig;

    public void incrementRateLimitBreach() {
        metricsConfig.getRateLimitBreachesCounter().increment();
    }

    public void incrementInventoryDecrement() {
        metricsConfig.getInventoryDecrementsCounter().increment();
    }

    public void incrementIdempotencyHit() {
        metricsConfig.getIdempotencyHitsCounter().increment();
    }
}