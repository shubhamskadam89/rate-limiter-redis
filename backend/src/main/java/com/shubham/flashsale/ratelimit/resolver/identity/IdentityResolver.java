package com.shubham.flashsale.ratelimit.resolver.identity;

import jakarta.servlet.http.HttpServletRequest;

public interface IdentityResolver {
  RateLimitIdentity resolve(HttpServletRequest request);
}
