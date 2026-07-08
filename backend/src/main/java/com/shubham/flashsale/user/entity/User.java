package com.shubham.flashsale.user.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_email", columnList = "email"),
      @Index(name = "idx_uuid", columnList = "uuid"),
      @Index(name = "idx_deleted_at", columnList = "deleted_at")
    })
@AllArgsConstructor
public class User extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, name = "full_name")
  private String fullName;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserRole role = UserRole.USER;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isActive = true;
}
