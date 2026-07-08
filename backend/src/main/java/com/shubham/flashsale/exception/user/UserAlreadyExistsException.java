package com.shubham.flashsale.exception.user;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("User already exists with email: " + email);
  }
}
