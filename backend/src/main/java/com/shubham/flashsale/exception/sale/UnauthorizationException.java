package com.shubham.flashsale.exception.sale;

public class UnauthorizationException extends RuntimeException {
  public UnauthorizationException(String message) {
    super(message);
  }
}
