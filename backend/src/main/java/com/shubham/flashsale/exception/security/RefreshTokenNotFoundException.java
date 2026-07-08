package com.shubham.flashsale.exception.security;

public class RefreshTokenNotFoundException extends RuntimeException {

  public RefreshTokenNotFoundException() {
    super("Refresh token not found");
  }
}
