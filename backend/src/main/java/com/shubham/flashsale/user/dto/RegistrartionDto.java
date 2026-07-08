package com.shubham.flashsale.user.dto;

import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.entity.UserRole;
import java.io.Serializable;

/** DTO for {@link User} */
public record RegistrartionDto(String email, String password, UserRole role, String fullName)
    implements Serializable {}
