package com.shubham.flashsale.auth.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import com.shubham.flashsale.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_token", columnList = "token"),
      @Index(name = "idx_refresh_user", columnList = "user_id")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private Boolean isRevoked;
}
