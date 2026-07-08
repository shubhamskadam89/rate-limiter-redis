package com.shubham.flashsale.exception.purchase;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(String message) {
    super(message);
  }
}
