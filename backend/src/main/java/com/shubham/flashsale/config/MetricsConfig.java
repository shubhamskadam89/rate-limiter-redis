package com.shubham.flashsale.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MetricsConfig {

  private final Counter rateLimitBreachesCounter;
  private final Counter inventoryDecrementsCounter;
  private final Counter idempotencyHitsCounter;

  public MetricsConfig(MeterRegistry meterRegistry) {

    this.rateLimitBreachesCounter =
        Counter.builder("rate_limit_breaches_total")
            .description("Total number of rate limit violations")
            .register(meterRegistry);

    this.inventoryDecrementsCounter =
        Counter.builder("inventory_decrements_total")
            .description("Total successful inventory decrements")
            .register(meterRegistry);

    this.idempotencyHitsCounter =
        Counter.builder("idempotency_hits_total")
            .description("Total duplicate requests served from idempotency cache")
            .register(meterRegistry);
  }
}
