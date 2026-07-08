package com.shubham.flashsale.ratelimit.web;

import com.shubham.flashsale.ratelimit.dto.RateLimitResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RateLimitResponseWriter {

  public void write(HttpServletResponse response, RateLimitResult result) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

    if (result.retryAfterMs() != null) {
      long retryAfterSeconds = Math.max(1, result.retryAfterMs() / 1000);
      response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    }

    response.setContentType("text/plain");
    response.getWriter().write("Too many requests");
  }
}
