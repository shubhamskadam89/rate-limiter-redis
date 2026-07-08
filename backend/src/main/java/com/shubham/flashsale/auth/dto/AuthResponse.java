package com.shubham.flashsale.auth.dto;

public record AuthResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn, String userRole) {}
