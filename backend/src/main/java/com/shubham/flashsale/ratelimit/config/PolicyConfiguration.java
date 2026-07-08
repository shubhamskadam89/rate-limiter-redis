package com.shubham.flashsale.ratelimit.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyConfiguration {

  private RateLimitAlgorithm algorithm;

  private long requests;

  private long window;

  private Long burst;

  private Double refill;
}
