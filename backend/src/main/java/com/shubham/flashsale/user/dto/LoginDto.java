package com.shubham.flashsale.user.dto;

import java.io.Serializable;

/** DTO for {@link com.shubham.flashsale.user.entity.User} */
public record LoginDto(String email, String password) implements Serializable {}
