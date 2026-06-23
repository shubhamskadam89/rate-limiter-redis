package com.shubham.flashsale.auth.controller;

import com.shubham.flashsale.auth.service.AuthService;
import com.shubham.flashsale.auth.dto.AuthResponse;
import com.shubham.flashsale.auth.dto.RefreshRequest;
import com.shubham.flashsale.user.dto.LoginDto;
import com.shubham.flashsale.user.dto.UserResponseDto;
import com.shubham.flashsale.user.entity.RegistrartionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/v1/register")
    ResponseEntity<UserResponseDto> registerUser(@RequestBody RegistrartionDto registrartionDto){
        return ResponseEntity.ok(authService.registerUser(registrartionDto));
    }

    @PostMapping("/v1/login")
    ResponseEntity<AuthResponse> loginUser(@RequestBody LoginDto loginDto){
        return  ResponseEntity.ok(authService.loginUser(loginDto));
    }

    @PostMapping("/v1/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshRequest request
    ){
        return ResponseEntity.ok(
                authService.refresh(request)
        );
    }

    @PostMapping("/v1/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshRequest request
    ){
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public String me(Authentication authentication){
        return authentication.getName();
    }


}