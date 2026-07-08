package com.shubham.flashsale.ratelimit.resolver.identity;

public record RateLimitIdentity(IdentityType type, String value) {
  public String key() {
    return type.name().toLowerCase() + ":" + value;
  }
}
