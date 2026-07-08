package com.shubham.flashsale.ratelimit.annotation;

import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
  RateLimitPolicy policy();
}
