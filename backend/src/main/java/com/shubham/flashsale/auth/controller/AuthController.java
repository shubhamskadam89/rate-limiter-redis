package com.shubham.flashsale.auth.controller;

import com.shubham.flashsale.auth.dto.AuthResponse;
import com.shubham.flashsale.auth.dto.RefreshRequest;
import com.shubham.flashsale.auth.service.AuthService;
import com.shubham.flashsale.user.dto.LoginDto;
import com.shubham.flashsale.user.dto.RegistrartionDto;
import com.shubham.flashsale.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @RequestBody RegistrartionDto registrationDto) {

        return ResponseEntity.ok(
                authService.registerUser(registrationDto)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @RequestBody LoginDto loginDto) {

        return ResponseEntity.ok(
                authService.loginUser(loginDto)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshRequest request) {

        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }
}