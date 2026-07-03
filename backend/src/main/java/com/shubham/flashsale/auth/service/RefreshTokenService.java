package com.shubham.flashsale.auth.service;

import com.shubham.flashsale.auth.entity.RefreshToken;
import com.shubham.flashsale.auth.repository.RefreshTokenRepository;
import com.shubham.flashsale.exception.security.RefreshTokenExpiredException;
import com.shubham.flashsale.exception.security.RefreshTokenNotFoundException;
import com.shubham.flashsale.exception.security.RefreshTokenRevokedException;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_DAYS = 30;

    public RefreshToken createRefreshToken(User user) {

        log.info("Creating refresh token for user uuid={}", user.getUuid());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(
                        LocalDateTime.now()
                                .plusDays(REFRESH_TOKEN_DAYS)
                )
                .isRevoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created successfully id={}", saved.getId());
        return saved;
    }
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(
                                RefreshTokenNotFoundException::new
                        );

        if(Boolean.TRUE.equals(refreshToken.getIsRevoked())){
            log.warn("Validation failed: Refresh token is revoked for token={}", token);
            throw new RefreshTokenRevokedException();
        }

        if(refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())){
            log.warn("Validation failed: Refresh token is expired for token={}", token);
            throw new RefreshTokenExpiredException();
        }

        log.debug("Refresh token validated successfully for user uuid={}", refreshToken.getUser().getUuid());
        return refreshToken;
    }
    public void revokeRefreshToken(
            RefreshToken refreshToken
    ){
        log.info("Revoking refresh token id={}", refreshToken.getId());
        refreshToken.setIsRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken rotateRefreshToken(
            RefreshToken oldToken
    ){
        log.info("Rotating refresh token id={}", oldToken.getId());

        revokeRefreshToken(oldToken);

        return createRefreshToken(
                oldToken.getUser()
        );
    }
}