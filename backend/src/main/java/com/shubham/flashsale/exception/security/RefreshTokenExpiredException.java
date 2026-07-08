package com.shubham.flashsale.exception.security;

public class RefreshTokenExpiredException extends RuntimeException {

  public RefreshTokenExpiredException() {
    super("Refresh token expired");
  }
}
