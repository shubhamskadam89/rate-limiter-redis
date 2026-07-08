package com.shubham.flashsale.exception.purchase;

public class PurchaseNotAllowedException extends RuntimeException {

  public PurchaseNotAllowedException() {
    super("Purchase is not allowed.");
  }
}
