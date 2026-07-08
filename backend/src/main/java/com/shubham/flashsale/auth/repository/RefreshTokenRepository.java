package com.shubham.flashsale.auth.repository;

import com.shubham.flashsale.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);
}
