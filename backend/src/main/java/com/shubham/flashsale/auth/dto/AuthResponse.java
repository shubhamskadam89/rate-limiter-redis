package com.shubham.flashsale.auth.dto;

import lombok.Data;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}