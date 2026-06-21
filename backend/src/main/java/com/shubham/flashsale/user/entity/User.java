package com.shubham.flashsale.user.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_uuid", columnList = "uuid"),
                @Index(name = "idx_deleted_at", columnList = "deleted_at")
        }
)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private Boolean isActive = true;
}