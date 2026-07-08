package com.shubham.flashsale.auth.controller;

import com.shubham.flashsale.auth.service.AuthService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import com.shubham.flashsale.user.dto.RegistrartionDto;
import com.shubham.flashsale.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/setup")
@RequiredArgsConstructor
@Profile("!prod")
public class SetupAuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @RateLimit(policy = RateLimitPolicy.AUTH)
  public ResponseEntity<UserResponseDto> registerSetupUser(
      @RequestBody RegistrartionDto registrationDto) {

    return ResponseEntity.ok(authService.registerUser(registrationDto));
  }
}
