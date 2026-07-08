package com.shubham.flashsale.user.dto;

import com.shubham.flashsale.user.entity.UserRole;
import java.io.Serializable;
import java.util.UUID;

/** DTO for {@link com.shubham.flashsale.user.entity.User} */
public record UserResponseDto(UUID uuid, String name, String email, UserRole role, Boolean isActive)
    implements Serializable {}
