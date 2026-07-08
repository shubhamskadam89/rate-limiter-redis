package com.shubham.flashsale.auth.controller;

import com.shubham.flashsale.auth.dto.AuthResponse;
import com.shubham.flashsale.auth.dto.RefreshRequest;
import com.shubham.flashsale.auth.service.AuthService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import com.shubham.flashsale.user.dto.LoginDto;
import com.shubham.flashsale.user.dto.RegistrartionDto;
import com.shubham.flashsale.user.dto.UserResponseDto;
import com.shubham.flashsale.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @RateLimit(policy = RateLimitPolicy.AUTH)
  public ResponseEntity<UserResponseDto> registerUser(
      @RequestBody RegistrartionDto registrationDto) {

    RegistrartionDto userRegistration =
        new RegistrartionDto(
            registrationDto.email(),
            registrationDto.password(),
            UserRole.USER,
            registrationDto.fullName());

    return ResponseEntity.ok(authService.registerUser(userRegistration));
  }

  @PostMapping("/login")
  @RateLimit(policy = RateLimitPolicy.AUTH)
  public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginDto loginDto) {

    return ResponseEntity.ok(authService.loginUser(loginDto));
  }

  @PostMapping("/refresh")
  @RateLimit(policy = RateLimitPolicy.AUTH)
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {

    return ResponseEntity.ok(authService.refresh(request));
  }

  @PostMapping("/logout")
  @RateLimit(policy = RateLimitPolicy.AUTH)
  public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {

    authService.logout(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> me() {
    return ResponseEntity.ok(authService.getMe());
  }
}
