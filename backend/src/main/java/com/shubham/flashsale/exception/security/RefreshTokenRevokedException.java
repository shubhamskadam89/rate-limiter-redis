package com.shubham.flashsale.exception.security;

public class RefreshTokenRevokedException extends RuntimeException {

  public RefreshTokenRevokedException() {
    super("Refresh token revoked");
  }
}
