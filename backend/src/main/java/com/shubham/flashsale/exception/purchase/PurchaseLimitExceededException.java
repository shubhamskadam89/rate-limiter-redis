package com.shubham.flashsale.exception.purchase;

public class PurchaseLimitExceededException extends RuntimeException {
  public PurchaseLimitExceededException() {
    super("Purchase limit exceeded");
  }
}
