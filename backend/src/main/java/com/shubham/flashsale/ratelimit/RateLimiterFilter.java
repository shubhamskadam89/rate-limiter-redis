package com.shubham.flashsale.ratelimit;

import com.shubham.flashsale.common.service.MetricsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final MetricsService metricsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        RateLimitResult result = rateLimiterService.checkLimit((request));

        if(!result.allowed()){

            log.warn("Rate limit breached for request URI={}, IP={}", request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            metricsService.incrementRateLimitBreach();
            response.getWriter().write("Too many Requests");
            return;
        }

        log.debug("Rate limit passed for URI={}", request.getRequestURI());
        filterChain.doFilter(request,response);


    }
}
