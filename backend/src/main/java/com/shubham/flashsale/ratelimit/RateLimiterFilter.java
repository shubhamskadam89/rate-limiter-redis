package com.shubham.flashsale.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//
//        return !request.getRequestURI().startsWith("/products");
//    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        RateLimitResult result = rateLimiterService.checkLimit(("demo-user"));

        if(!result.allowed()){

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many Requests");
            return;
        }

        filterChain.doFilter(request,response);


    }
}
