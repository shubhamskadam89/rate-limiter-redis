package com.shubham.flashsale.ratelimit.resolver.policy;

import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class RateLimitPolicyResolver {

  public RateLimitPolicy resolve(HandlerMethod handlerMethod) {

    // 1. Method annotation takes precedence
    RateLimit methodAnnotation =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RateLimit.class);

    if (methodAnnotation != null) {
      return methodAnnotation.policy();
    }

    // 2. Fall back to controller class annotation
    RateLimit classAnnotation =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RateLimit.class);

    if (classAnnotation != null) {
      return classAnnotation.policy();
    }

    // 3. Safe default
    return RateLimitPolicy.GENERAL;
  }
}
