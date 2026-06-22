package com.shubham.flashsale.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final RateLimiterService rateLimiterService;

    @GetMapping("/limit")
    public RateLimitResult test() {

        return rateLimiterService
                .checkLimit("demo-user");
    }
}