package com.shubham.flashsale.exception.purchase;

public class SoldOutException extends RuntimeException {

  public SoldOutException() {
    super("Sale item is sold out");
  }
}
