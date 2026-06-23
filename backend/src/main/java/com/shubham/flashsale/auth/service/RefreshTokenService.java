package com.shubham.flashsale.auth.service;

import com.shubham.flashsale.auth.entity.RefreshToken;
import com.shubham.flashsale.auth.repository.RefreshTokenRepository;
import com.shubham.flashsale.exception.RefreshTokenExpiredException;
import com.shubham.flashsale.exception.RefreshTokenNotFoundException;
import com.shubham.flashsale.exception.RefreshTokenRevokedException;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_DAYS = 30;

    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(
                        LocalDateTime.now()
                                .plusDays(REFRESH_TOKEN_DAYS)
                )
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(
                                RefreshTokenNotFoundException::new
                        );

        if(Boolean.TRUE.equals(refreshToken.getIsRevoked())){
            throw new RefreshTokenRevokedException();
        }

        if(refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())){
            throw new RefreshTokenExpiredException();
        }

        return refreshToken;
    }
    public void revokeRefreshToken(
            RefreshToken refreshToken
    ){
        refreshToken.setIsRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken rotateRefreshToken(
            RefreshToken oldToken
    ){

        revokeRefreshToken(oldToken);

        return createRefreshToken(
                oldToken.getUser()
        );
    }
}